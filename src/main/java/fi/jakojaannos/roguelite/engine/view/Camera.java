package fi.jakojaannos.roguelite.engine.view;

import org.joml.Vector2f;

public class Camera {
    private final Vector2f position;

    public final float getX() {
        return this.position.x;
    }

    public final float getY() {
        return this.position.y;
    }

    public final void setX(float x) {
        setPosition(x, getY());
    }

    public final void setY(float y) {
        setPosition(getX(), y);
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public Camera(Vector2f position) {
        this.position = new Vector2f(position);
    }
}
