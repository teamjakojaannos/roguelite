package fi.jakojäännös.roguelite.game;

import fi.jakojäännös.roguelite.engine.GameBase;
import fi.jakojäännös.roguelite.engine.input.InputEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Queue;

@Slf4j
public class Roguelite extends GameBase {
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputUp;
    private boolean inputDown;

    @Getter private float playerX = 4.0f;
    @Getter private float playerY = 4.0f;
    @Getter private float playerSpeed = 8.0f;
    @Getter private float playerSize = 1.0f;

    @Override
    public void tick(Queue<InputEvent> inputEvents, double delta) {
        super.tick(inputEvents, delta);

        while (!inputEvents.isEmpty()) {
            val event = inputEvents.remove();

            if (event.getKey() == InputEvent.Key.KEY_A) {
                this.inputLeft = event.getAction() != InputEvent.Action.RELEASE;
            } else if (event.getKey() == InputEvent.Key.KEY_D) {
                this.inputRight = event.getAction() != InputEvent.Action.RELEASE;
            } else if (event.getKey() == InputEvent.Key.KEY_W) {
                this.inputUp = event.getAction() != InputEvent.Action.RELEASE;
            } else if (event.getKey() == InputEvent.Key.KEY_S) {
                this.inputDown = event.getAction() != InputEvent.Action.RELEASE;
            }
        }

        val playerDirectionMultiplierX = (this.inputRight ? 1 : 0) - (this.inputLeft ? 1 : 0);
        val playerDirectionMultiplierY = (this.inputDown ? 1 : 0) - (this.inputUp ? 1 : 0);
        val playerVelocityX = this.playerSpeed * playerDirectionMultiplierX;
        val playerVelocityY = this.playerSpeed * playerDirectionMultiplierY;
        this.playerX += playerVelocityX * delta;
        this.playerY += playerVelocityY * delta;
    }
}
