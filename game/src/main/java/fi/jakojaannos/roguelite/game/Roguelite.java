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
                .withSystem("simple_collision_handler", new ProjectileToCharacterCollisionHandlerSystem(), "simple_collision")
                .withSystem("collision_event_remover", new CollisionEventRemoverSystem(), "simple_collision_handler")
                .build();
    }

    public static GameState createInitialState() {
        val entities = Entities.createNew(256, 32);
        val state = new GameState(World.createNew(entities));

        val player = entities.createEntity();
        entities.addComponentTo(player, new Transform(4.0f, 4.0f));
        entities.addComponentTo(player, new Velocity());
        entities.addComponentTo(player, new CharacterInput());
        entities.addComponentTo(player, new CharacterAbilities());
        entities.addComponentTo(player, new CharacterStats(
                10.0f,
                100.0f,
                150.0f,
                20.0f,
                20.0f
        ));
        entities.addComponentTo(player, new PlayerTag());
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/sheep";
        entities.addComponentTo(player, sprite);
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

        // Spawn "followers"
        final double x_max = 20.0f, y_max = 15.0f;
        Random random = new Random(123);

        for (int i = 0; i < 0; i++) {
            var e = entities.createEntity();
            double xpos = random.nextDouble() * x_max;
            double ypos = random.nextDouble() * y_max;
            entities.addComponentTo(e, new Transform(xpos, ypos));
            entities.addComponentTo(e, new Velocity());
            entities.addComponentTo(e, new CharacterInput());
            entities.addComponentTo(e, new CharacterStats(
                    4.0,
                    100.0,
                    800.0,
                    4.0,
                    20.0
            ));

            entities.addComponentTo(e, new EnemyAI(25.0f, 1.0f));
        }


        // Spawn stalker(s)
        /*val e = entities.createEntity();
        entities.addComponentTo(e, new Transform(10.0f, 15.0f, 0.75f));
        entities.addComponentTo(e, new Velocity());
        entities.addComponentTo(e, new CharacterInput());
        entities.addComponentTo(e, new CharacterStats(
                1.0,
                100.0,
                800.0,
                4.0,
                20.0
        ));
        entities.addComponentTo(e,
                new StalkerAI(250.0f, 50.0f, 8.0f));*/

        // Create spawner

        //var spawn = entities.createEntity();
        //entities.addComponentTo(spawn, new Transform(5.0f, 15.0f, 0.5f));
        //entities.addComponentTo(spawn, new SpawnerComponent(0.7f, SpawnerComponent.FACTORY_DUMMY, 5.0f, 543));

        //var spawn2 = entities.createEntity();
        //entities.addComponentTo(spawn2, new Transform(15.0f, 5.0f, 0.5f));
        //entities.addComponentTo(spawn2, new SpawnerComponent(2.0f, SpawnerComponent.FACTORY_FOLLOWER, 2.0f, 123));

        // create dummy targets
        for (int i = 0; i < 5; i++) {
            Entity dummy = entities.createEntity();
            entities.addComponentTo(dummy, new Transform(3.0f + i*2.0f, 5.0f));
            entities.addComponentTo(dummy, new ColliderTag());
        }

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
        entities.addComponentTo(levelEntity, new Level(tileMap));

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
        val entities = state.getWorld().getEntities();
        val inputs = state.getWorld().getResource(Inputs.class);
        val mouse = state.getWorld().getResource(Mouse.class);
        entities.applyModifications();

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
    }
}
