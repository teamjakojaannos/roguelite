package fi.jakojäännös.roguelite.game.data;

public class GameState {
    public int viewWidth = 32;

    public boolean inputLeft;
    public boolean inputRight;
    public boolean inputUp;
    public boolean inputDown;

    public float playerX = 4.0f;
    public float playerY = 4.0f;
    public float playerSpeed = 8.0f;
    public float playerSize = 1.0f;

    public float mouseX;
    public float mouseY;
}
