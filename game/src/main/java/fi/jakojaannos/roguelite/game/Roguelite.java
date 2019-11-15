package fi.jakojaannos.roguelite.game;

import fi.jakojaannos.roguelite.engine.GameBase;
import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.input.ButtonInput;
import fi.jakojaannos.roguelite.engine.input.InputAxis;
import fi.jakojaannos.roguelite.engine.input.InputButton;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.systems.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Queue;

@Slf4j
public class Roguelite extends GameBase<GameState> {
    private final SystemDispatcher<GameState> dispatcher;

    public Roguelite() {
        this.dispatcher = new DispatcherBuilder<GameState>()
                .withSystem("player_input", new PlayerInputSystem())
                .withSystem("projectile_move", new ProjectileMovementSystem())
                .withSystem("character_move", new CharacterMovementSystem(), "player_input")
                .withSystem("character_attack", new CharacterAttackSystem(), "player_input")
                .withSystem("crosshair_snap_to_cursor", new SnapToCursorSystem())
                .build();
    }

    public static Cluster createCluster(int capacity) {
        val cluster = new Cluster(capacity);
        cluster.registerComponentType(Transform.class, Transform[]::new);
        cluster.registerComponentType(Velocity.class, Velocity[]::new);
        cluster.registerComponentType(CharacterInput.class, CharacterInput[]::new);
        cluster.registerComponentType(CharacterStats.class, CharacterStats[]::new);
        cluster.registerComponentType(CharacterAbilities.class, CharacterAbilities[]::new);
        cluster.registerComponentType(PlayerTag.class, PlayerTag[]::new);
        cluster.registerComponentType(CrosshairTag.class, CrosshairTag[]::new);
        cluster.registerComponentType(ProjectileTag.class, ProjectileTag[]::new);

        return cluster;
    }

    public static GameState createInitialState() {
        val state = new GameState(createCluster(256));
        state.player = state.world.createEntity();
        state.world.addComponentTo(state.player, new Transform(4.0f, 4.0f));
        state.world.addComponentTo(state.player, new Velocity());
        state.world.addComponentTo(state.player, new CharacterInput());
        state.world.addComponentTo(state.player, new CharacterAbilities());
        state.world.addComponentTo(state.player, new CharacterStats(
                10.0f,
                100.0f,
                800.0f,
                20.0f,
                20.0f
        ));
        state.world.addComponentTo(state.player, new PlayerTag());

        state.crosshair = state.world.createEntity();
        state.world.addComponentTo(state.crosshair, new Transform(-999.0, -999.0, 0.5, 0.5, 0.25, 0.25));
        state.world.addComponentTo(state.crosshair, new CrosshairTag());

        return state;
    }

    @Override
    public void tick(
            @NonNull GameState state,
            @NonNull Queue<InputEvent> inputEvents,
            double delta
    ) {
        super.tick(state, inputEvents, delta);
        state.world.applyModifications();

        while (!inputEvents.isEmpty()) {
            val event = inputEvents.remove();

            event.getAxis().ifPresent(input -> {
                if (input.getAxis() == InputAxis.Mouse.X_POS) {
                    state.mouseX = input.getValue();
                } else if (input.getAxis() == InputAxis.Mouse.Y_POS) {
                    state.mouseY = input.getValue();
                }
            });

            event.getButton().ifPresent(input -> {
                if (input.getButton() == InputButton.Keyboard.KEY_A) {
                    state.inputLeft = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_D) {
                    state.inputRight = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_W) {
                    state.inputUp = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_S) {
                    state.inputDown = input.getAction() != ButtonInput.Action.RELEASE;
                }
            });
        }

        this.dispatcher.dispatch(state.world, state, delta);
    }
}
