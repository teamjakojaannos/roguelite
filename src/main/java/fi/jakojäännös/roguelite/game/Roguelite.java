package fi.jakojäännös.roguelite.game;

import fi.jakojäännös.roguelite.engine.GameBase;
import fi.jakojäännös.roguelite.game.data.GameState;
import fi.jakojäännös.roguelite.engine.input.ButtonInput;
import fi.jakojäännös.roguelite.engine.input.InputAxis;
import fi.jakojäännös.roguelite.engine.input.InputButton;
import fi.jakojäännös.roguelite.engine.input.InputEvent;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Queue;

@Slf4j
public class Roguelite extends GameBase<GameState> {
    @Override
    public void tick(GameState state, Queue<InputEvent> inputEvents, double delta) {
        super.tick(state, inputEvents, delta);

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
        state.playerX += playerVelocityX * delta;
        state.playerY += playerVelocityY * delta;

        state.crosshairX = state.mouseX * state.realViewWidth;
        state.crosshairY = state.mouseY * state.realViewHeight;
    }
}
