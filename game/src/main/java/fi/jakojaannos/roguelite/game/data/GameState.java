package fi.jakojaannos.roguelite.game.data;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.Entity;

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

    public float playerSpeed = 8.0f;
    public float playerSize = 1.0f;

    public float crosshairSize = 0.5f;

    public Cluster world;
    public Entity player;
    public Entity crosshair;
}
