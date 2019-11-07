package fi.jakojäännös.roguelite.game.data;

public class GameState {
    public float targetWorldSize = 32.0f;

    public float realViewWidth = 1.0f;
    public float realViewHeight = 1.0f;

    public float mouseX = -999.0f;
    public float mouseY = -999.0f;

    public boolean inputLeft = false;
    public boolean inputRight = false;
    public boolean inputUp = false;
    public boolean inputDown = false;

    public float playerX = 4.0f;
    public float playerY = 4.0f;
    public float playerSpeed = 8.0f;
    public float playerSize = 1.0f;

    public float crosshairX = -999.0f;
    public float crosshairY = -999.0f;
    public float crosshairSize = 0.5f;
}
