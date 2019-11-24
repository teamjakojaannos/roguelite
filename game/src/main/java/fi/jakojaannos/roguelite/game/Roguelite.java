package fi.jakojaannos.roguelite.game;

import fi.jakojaannos.roguelite.engine.GameBase;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.input.ButtonInput;
import fi.jakojaannos.roguelite.engine.input.InputAxis;
import fi.jakojaannos.roguelite.engine.input.InputButton;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.engine.utilities.GenerateStream;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.archetypes.DummyArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.FollowerArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.PlayerArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.StalkerArchetype;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Queue;
import java.util.Random;

@Slf4j
public class Roguelite extends GameBase<GameState> {
    private final SystemDispatcher dispatcher;

    public Roguelite() {
        this.dispatcher = new DispatcherBuilder()
                .withSystem("player_input", new PlayerInputSystem())
                .withSystem("projectile_move", new ProjectileMovementSystem())
                .withSystem("character_move", new CharacterMovementSystem(), "player_input")
                .withSystem("character_attack", new CharacterAttackSystem(), "player_input")
                .withSystem("crosshair_snap_to_cursor", new SnapToCursorSystem())
                .withSystem("ai_move", new CharacterAIControllerSystem(), "character_move")
                .withSystem("stalker_move", new StalkerAIControllerSystem())
                .withSystem("camera", new CameraControlSystem(), "character_move")
                .withSystem("spawner", new SpawnerSystem())
                .withSystem("simple_collision", new SimpleColliderSystem())
                .withSystem("tile_collision", new TileMapCollisionSystem())
                .withSystem("simple_collision_handler", new ProjectileToCharacterCollisionHandlerSystem(), "simple_collision", "tile_collision")
                .withSystem("character_to_tile_collision_handler", new CharacterToTileMapCollisionSystem(), "simple_collision", "tile_collision")
                .withSystem("collision_event_remover", new CollisionEventCleanupSystem(), "simple_collision_handler")
                .withSystem("post_tick_physics", new PostUpdatePhysicsSystem(), "collision_event_remover")
                .withSystem("health_check", new HealthCheckSystem(), "character_attack")
                .build();
    }

    public static GameState createInitialState() {
        val entities = Entities.createNew(256, 32);
        val state = new GameState(World.createNew(entities));

        val player = PlayerArchetype.create(entities,
                new Transform(4.0, 4.0));
        state.getWorld().getResource(Players.class).player = player;

        val camera = entities.createEntity();
        val camComponent = new Camera();
        camComponent.followTarget = player;
        entities.addComponentTo(camera, camComponent);
        entities.addComponentTo(camera, new NoDrawTag());
        state.getWorld().getResource(CameraProperties.class).cameraEntity = camera;

        val crosshair = entities.createEntity();
        entities.addComponentTo(crosshair, new Transform(-999.0, -999.0, 0.5, 0.5, 0.25, 0.25));
        entities.addComponentTo(crosshair, new CrosshairTag());


        FollowerArchetype.create(entities, new Transform(5.0f, 1.0f));
        StalkerArchetype.create(entities, new Transform(1.0f, 2.0f));
        DummyArchetype.create(entities, new Transform(2.0f, 7.0f));
        DummyArchetype.create(entities, new Transform(3.0f, 7.0f));
        DummyArchetype.create(entities, new Transform(4.0f, 7.0f));
        DummyArchetype.create(entities, new Transform(5.0f, 7.0f));

        val spawner_stalker = entities.createEntity();
        entities.addComponentTo(spawner_stalker, new Transform(15.0f, 15.0f));
        entities.addComponentTo(spawner_stalker, new SpawnerComponent(
                10.0f,
                SpawnerComponent.FACTORY_STALKER)
        );

        val spawner_follower = entities.createEntity();
        entities.addComponentTo(spawner_follower, new Transform(2.0f, 15.0f));
        entities.addComponentTo(spawner_follower, new SpawnerComponent(
                5.0f,
                SpawnerComponent.FACTORY_FOLLOWER)
        );


        val emptiness = new TileType(0, false);
        val floor = new TileType(1, false);
        val wall = new TileType(2, true);
        val tileMap = new TileMap<TileType>(emptiness);

        val startX = -25;
        val startY = -25;
        val roomWidth = 50;
        val roomHeight = 50;
        GenerateStream.ofCoordinates(startX, startY, roomWidth, roomHeight)
                .filter(pos -> pos.x == startX + roomWidth - 1 || pos.x == startX || pos.y == startY + roomHeight - 1 || pos.y == startY)
                .forEach(pos -> tileMap.setTile(pos, wall));
        GenerateStream.ofCoordinates(startX + 1, startY + 1, roomWidth - 2, roomHeight - 2)
                .forEach(pos -> tileMap.setTile(pos, floor));

        val levelEntity = entities.createEntity();
        entities.addComponentTo(levelEntity, new TileMapLayer(tileMap));

        entities.applyModifications();
        return state;
    }

    @Override
    public void tick(
            @NonNull GameState state,
            @NonNull Queue<InputEvent> inputEvents,
            double delta
    ) {
        super.tick(state, inputEvents, delta);
        val inputs = state.getWorld().getResource(Inputs.class);
        val mouse = state.getWorld().getResource(Mouse.class);

        while (!inputEvents.isEmpty()) {
            val event = inputEvents.remove();

            event.getAxis().ifPresent(input -> {
                if (input.getAxis() == InputAxis.Mouse.X_POS) {
                    mouse.pos.x = input.getValue();
                } else if (input.getAxis() == InputAxis.Mouse.Y_POS) {
                    mouse.pos.y = input.getValue();
                }
            });

            event.getButton().ifPresent(input -> {
                if (input.getButton() == InputButton.Keyboard.KEY_A) {
                    inputs.inputLeft = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_D) {
                    inputs.inputRight = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_W) {
                    inputs.inputUp = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_S) {
                    inputs.inputDown = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Mouse.button(0)) {
                    inputs.inputAttack = input.getAction() != ButtonInput.Action.RELEASE;
                }
            });
        }

        this.dispatcher.dispatch(state.getWorld(), delta);
        state.getWorld().getEntities().applyModifications();
    }
}
