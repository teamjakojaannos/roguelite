package fi.jakojaannos.roguelite.engine.utilities.math;

import lombok.NoArgsConstructor;
import org.joml.Vector2d;

@NoArgsConstructor
public final class RotatedRectangle {
    public double width = 1.0;
    public double height = 1.0;
    public Vector2d originOffset = new Vector2d();
    public Vector2d position = new Vector2d();
    public double rotation = 0.0;

    public RotatedRectangle(
            final Vector2d position,
            final Vector2d originOffset,
            final double width,
            final double height,
            final double rotation
    ) {
        set(position, originOffset, width, height, rotation);
    }

    public void set(
            final Vector2d position,
            final Vector2d originOffset,
            final double width,
            final double height,
            final double rotation
    ) {
        set(position.x, position.y, originOffset.x, originOffset.y, width, height, rotation);
    }

    public void set(
            final double x,
            final double y,
            final double originX,
            final double originY,
            final double width,
            final double height,
            final double rotation
    ) {
        this.position.set(x, y);
        this.originOffset.set(originX, originY);
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
                                                    x - this.originOffset.x,
                                                    y - this.originOffset.y,
                                                    result);
    }

}
