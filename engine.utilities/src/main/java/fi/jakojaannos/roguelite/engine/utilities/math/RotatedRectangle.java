package fi.jakojaannos.roguelite.engine.utilities.math;

import org.joml.Vector2d;

public final class RotatedRectangle {
    public double width;
    public double height;
    public Vector2d originOffset = new Vector2d();
    public Vector2d position = new Vector2d();
    public double rotation;

    public RotatedRectangle(
            final Vector2d position,
            final Vector2d originOffset,
            final double width,
            final double height,
            final double rotation
    ) {
        this.position.set(position);
        this.originOffset.set(originOffset);
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    public Vector2d getTopLeft(final Vector2d result) {
        return getRelative(this.position.x,
                           this.position.y,
                           result);
    }

    public Vector2d getTopRight(final Vector2d result) {
        return getRelative(this.position.x + this.width,
                           this.position.y,
                           result);
    }

    public Vector2d getBottomLeft(final Vector2d result) {
        return getRelative(this.position.x,
                           this.position.y + this.height,
                           result);
    }

    public Vector2d getBottomRight(final Vector2d result) {
        return getRelative(this.position.x + this.width,
                           this.position.y + this.height,
                           result);
    }

    public Vector2d getRelative(final double x, final double y, final Vector2d result) {
        return CoordinateHelper.transformCoordinate(this.position.x - this.originOffset.x,
                                                    this.position.y - this.originOffset.y,
                                                    this.rotation,
                                                    x,
                                                    y,
                                                    result);
    }

}
