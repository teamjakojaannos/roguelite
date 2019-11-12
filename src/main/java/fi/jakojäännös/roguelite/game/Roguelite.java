package fi.jakojäännös.roguelite.game;

import fi.jakojäännös.roguelite.engine.GameBase;
import fi.jakojäännös.roguelite.engine.ecs.*;
import fi.jakojäännös.roguelite.game.data.GameState;
import fi.jakojäännös.roguelite.engine.input.ButtonInput;
import fi.jakojäännös.roguelite.engine.input.InputAxis;
import fi.jakojäännös.roguelite.engine.input.InputButton;
import fi.jakojäännös.roguelite.engine.input.InputEvent;
import fi.jakojäännös.roguelite.game.data.components.*;
import fi.jakojäännös.roguelite.game.systems.CharacterMovementSystem;
import fi.jakojäännös.roguelite.game.systems.PlayerInputSystem;
import fi.jakojäännös.roguelite.game.systems.SnapToCursorSystem;
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
                .withSystem("character_move", new CharacterMovementSystem(), "player_input")
                .withSystem("crosshair_snap_to_cursor", new SnapToCursorSystem())
                .build();
    }

    public GameState createInitialState() {
        val state = new GameState();
        state.world = new Cluster(256);
        state.world.registerComponentType(Position.class, Position[]::new);
        state.world.registerComponentType(Velocity.class, Velocity[]::new);
        state.world.registerComponentType(CharacterInput.class, CharacterInput[]::new);
        state.world.registerComponentType(CharacterStats.class, CharacterStats[]::new);
        state.world.registerComponentType(PlayerTag.class, PlayerTag[]::new);
        state.world.registerComponentType(CrosshairTag.class, CrosshairTag[]::new);

        state.player = state.world.createEntity();
        state.world.addComponentTo(state.player, new Position(4.0f, 4.0f));
        state.world.addComponentTo(state.player, new Velocity());
        state.world.addComponentTo(state.player, new CharacterInput());
        state.world.addComponentTo(state.player, new CharacterStats(
                10.0f,
                100.0f,
                800.0f
        ));
        state.world.addComponentTo(state.player, new PlayerTag());

        state.crosshair = state.world.createEntity();
        state.world.addComponentTo(state.crosshair, new Position(-999.0f, -999.0f));
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
