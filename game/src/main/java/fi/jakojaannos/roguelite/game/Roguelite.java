package fi.jakojaannos.roguelite.game;

import fi.jakojaannos.roguelite.engine.GameBase;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.input.ButtonInput;
import fi.jakojaannos.roguelite.engine.input.InputAxis;
import fi.jakojaannos.roguelite.engine.input.InputButton;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.archetypes.PlayerArchetype;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.*;
import fi.jakojaannos.roguelite.game.systems.collision.*;
import fi.jakojaannos.roguelite.game.world.WorldGenerator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
import java.util.Queue;

@Slf4j
public class Roguelite extends GameBase<GameState> {
    private final SystemDispatcher dispatcher;

    public Roguelite() {
        this.dispatcher = SystemDispatcher
                .builder()
                .withGroups(SystemGroups.values())
                .addGroupDependencies(SystemGroups.CLEANUP, Arrays.stream(SystemGroups.values())
                                                                  .filter(group -> group != SystemGroups.CLEANUP)
                                                                  .toArray(SystemGroup[]::new))
                .addGroupDependencies(SystemGroups.EARLY_TICK, SystemGroups.INPUT)
                .addGroupDependencies(SystemGroups.CHARACTER_TICK, SystemGroups.INPUT, SystemGroups.EARLY_TICK)
                .addGroupDependencies(SystemGroups.PHYSICS_TICK, SystemGroups.CHARACTER_TICK, SystemGroups.EARLY_TICK)
                .addGroupDependencies(SystemGroups.COLLISION_HANDLER, SystemGroups.PHYSICS_TICK)
                .addGroupDependencies(SystemGroups.LATE_TICK, SystemGroups.COLLISION_HANDLER, SystemGroups.PHYSICS_TICK, SystemGroups.CHARACTER_TICK)
                .withSystem(new ColliderDataCollectorSystem())
                .withSystem(new PlayerInputSystem())
                .withSystem(new CharacterMovementSystem())
                .withSystem(new CharacterAttackSystem())
                .withSystem(new ApplyVelocitySystem())
                .withSystem(new SnapToCursorSystem())
                .withSystem(new CharacterAIControllerSystem())
                .withSystem(new StalkerAIControllerSystem())
                .withSystem(new CameraControlSystem())
                .withSystem(new SpawnerSystem())
                .withSystem(new ProjectileToCharacterCollisionHandlerSystem())
                .withSystem(new DestroyProjectilesOnCollisionSystem())
                .withSystem(new CollisionEventCleanupSystem())
                .withSystem(new HealthUpdateSystem())
                .build();
    }

    public static GameState createInitialState() {
        return createInitialState(System.nanoTime());
    }

    public static GameState createInitialState(long seed) {
        val entities = EntityManager.createNew(256, 32);
        val state = new GameState(World.createNew(entities));

        val player = PlayerArchetype.create(entities,
                                            new Transform(0, 0));
        state.getWorld().getResource(Players.class).player = player;

        val camera = entities.createEntity();
        val camComponent = new Camera();
        camComponent.followTarget = player;
        entities.addComponentTo(camera, camComponent);
        entities.addComponentTo(camera, new NoDrawTag());
        state.getWorld().getResource(CameraProperties.class).cameraEntity = camera;

        val crosshair = entities.createEntity();
        entities.addComponentTo(crosshair, new Transform(-999.0, -999.0));
        entities.addComponentTo(crosshair, new CrosshairTag());
        val crosshairCollider = new Collider(CollisionLayer.NONE);
        crosshairCollider.width = 0.30;
        crosshairCollider.height = 0.30;
        crosshairCollider.origin.set(0.15);
        entities.addComponentTo(crosshair, crosshairCollider);

        val emptiness = new TileType(0, false);
        val floor = new TileType(1, false);
        val wall = new TileType(2, true);
        val generator = new WorldGenerator<TileType>(emptiness);
        generator.prepareInitialRoom(seed, state.getWorld(), floor, wall, 25, 45, 5, 5, 2);

        val levelEntity = entities.createEntity();
        val layer = new TileMapLayer(generator.getTileMap());
        layer.collisionEnabled = true;
        entities.addComponentTo(levelEntity, layer);

        entities.applyModifications();
        return state;
    }

    @Override
    public void tick(
            GameState state,
            Queue<InputEvent> inputEvents,
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

        if (getTime().getCurrentGameTime() % 100 == 0) {
            LOG.info("Entities: {}", state.getWorld().getEntityManager().entityCount());
        }
    }
}
