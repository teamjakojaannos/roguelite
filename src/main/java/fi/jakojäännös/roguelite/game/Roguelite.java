package fi.jakojäännös.roguelite.game;

import fi.jakojäännös.roguelite.engine.GameBase;
import fi.jakojäännös.roguelite.engine.ecs.Cluster;
import fi.jakojäännös.roguelite.game.data.GameState;
import fi.jakojäännös.roguelite.engine.input.ButtonInput;
import fi.jakojäännös.roguelite.engine.input.InputAxis;
import fi.jakojäännös.roguelite.engine.input.InputButton;
import fi.jakojäännös.roguelite.engine.input.InputEvent;
import fi.jakojäännös.roguelite.game.data.components.Position;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Queue;

@Slf4j
public class Roguelite extends GameBase<GameState> {
    public GameState createInitialState() {
        val state = new GameState();
        state.world = new Cluster();
        state.world.registerComponentType(Position.class, Position[]::new);

        state.player = state.world.createEntity();
        state.world.addComponentTo(state.player, new Position(4.0f, 4.0f));

        state.crosshair = state.world.createEntity();
        state.world.addComponentTo(state.crosshair, new Position(-999.0f, -999.0f));
        return state;
    }

    @Override
    public void tick(@NonNull GameState state, @NonNull Queue<InputEvent> inputEvents, double delta) {
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

        val playerDirectionMultiplierX = (state.inputRight ? 1 : 0) - (state.inputLeft ? 1 : 0);
        val playerDirectionMultiplierY = (state.inputDown ? 1 : 0) - (state.inputUp ? 1 : 0);
        val playerVelocityX = state.playerSpeed * playerDirectionMultiplierX;
        val playerVelocityY = state.playerSpeed * playerDirectionMultiplierY;
        state.world.getComponentOf(state.player, Position.class)
                   .ifPresent(position -> {
                       position.x += playerVelocityX * delta;
                       position.y += playerVelocityY * delta;
                   });

        state.world.getComponentOf(state.crosshair, Position.class)
                   .ifPresent(position -> {
                       position.x = state.mouseX * state.realViewWidth;
                       position.y = state.mouseY * state.realViewHeight;
                   });
    }
}
