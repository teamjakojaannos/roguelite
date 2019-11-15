package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import org.joml.Rectangled;
import org.joml.Vector2d;

public class Transform implements Component {
    public Rectangled bounds = new Rectangled();
    public Vector2d origin = new Vector2d();

    public final double getWidth() {
        return this.bounds.maxX - this.bounds.minX;
    }

    public final double getHeight() {
        return this.bounds.maxY - this.bounds.minY;
    }

    public Transform() {
        this(0.0, 0.0, 1.0);
    }

    public Transform(double x, double y) {
        this(x, y, 1.0);
    }

    public Transform(double x, double y, double size) {
        this(x, y, size, size);
    }

    public Transform(double x, double y, double w, double h) {
        this(x, y, w, h, 0.0, 0.0);
    }

    public Transform(double x, double y, double w, double h, double originX, double originY) {
        this.origin.x = originX;
        this.origin.y = originY;
        setPositionAndSize(x, y, w, h);
    }

    public void setPosition(double x, double y) {
        setPositionAndSize(x, y, getWidth(), getHeight());
    }

    public void setPositionAndSize(double x, double y, double w, double h) {
        this.bounds.minX = x - this.origin.x;
        this.bounds.minY = y - this.origin.y;
        this.bounds.maxX = x + w - this.origin.x;
        this.bounds.maxY = y + h - this.origin.y;
    }

    public double getCenterX() {
        return bounds.minX + getWidth() / 2;
    }

    public double getCenterY() {
        return bounds.minY + getHeight() / 2;
    }

    public void getCenter(Vector2d result) {
        result.x = getCenterX();
        result.y = getCenterY();
    }

}
