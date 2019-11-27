package fi.jakojaannos.roguelite.game;

import fi.jakojaannos.roguelite.engine.GameBase;
import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.input.ButtonInput;
import fi.jakojaannos.roguelite.engine.input.InputAxis;
import fi.jakojaannos.roguelite.engine.input.InputButton;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.archetypes.*;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.*;
import fi.jakojaannos.roguelite.game.world.WorldGenerator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Queue;

@Slf4j
public class Roguelite extends GameBase<GameState> {
    private final SystemDispatcher dispatcher;

    public Roguelite() {
        this.dispatcher = new DispatcherBuilder()
                .withSystem("player_input", new PlayerInputSystem())
                .withSystem("character_move", new CharacterMovementSystem(), "player_input")
                .withSystem("character_attack", new CharacterAttackSystem(), "player_input")
                .withSystem("process_move", new ApplyVelocitySystem(), "character_move", "character_attack")
                .withSystem("crosshair_snap_to_cursor", new SnapToCursorSystem())
                .withSystem("ai_move", new CharacterAIControllerSystem(), "character_move")
                .withSystem("stalker_move", new StalkerAIControllerSystem())
                .withSystem("camera", new CameraControlSystem(), "character_move")
                .withSystem("spawner", new SpawnerSystem())
                .withSystem("simple_collision_handler", new ProjectileToCharacterCollisionHandlerSystem(), "process_move")
                .withSystem("projectile_remover", new DestroyProjectilesOnCollisionSystem(), "simple_collision_handler")
                .withSystem("collision_event_remover", new CollisionEventCleanupSystem(), "simple_collision_handler", "projectile_remover")
                .withSystem("post_tick_physics", new PostUpdatePhysicsSystem(), "collision_event_remover")
                .withSystem("health_check", new HealthCheckSystem(), "character_attack")
                .build();
    }

    public static GameState createInitialState() {
        return createInitialState(System.nanoTime());
    }

    public static GameState createInitialState(long seed) {
        val entities = EntityManager.createNew(256, 32);
        val state = new GameState(World.createNew(entities));

        val player = PlayerArchetype.create(entities,
                                            new Transform(0, 0, 1.0, 1.0, 0.5, 0.5));
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

        val emptiness = new TileType(0, false);
        val floor = new TileType(1, false);
        val wall = new TileType(2, true);
        val generator = new WorldGenerator<TileType>(emptiness);
        generator.prepareInitialRoom(seed, state.getWorld(), floor, wall, 25, 45, 5, 5, 2);

        val levelEntity = entities.createEntity();
        entities.addComponentTo(levelEntity, new TileMapLayer(generator.getTileMap()));

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
        state.getWorld().getEntityManager().applyModifications();
    }
}
