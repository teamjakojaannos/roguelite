package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.utilities.math.RotatedRectangle;
import fi.jakojaannos.roguelite.game.data.collision.Collision;
import fi.jakojaannos.roguelite.game.data.collision.CollisionEvent;
import lombok.val;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Collider implements Component, Shape {
    public boolean solid;
    public boolean overlaps = true;
    public double width = 1.0;
    public double height = 1.0;
    public Vector2d origin = new Vector2d();
    public final List<CollisionEvent> collisions = new ArrayList<>();

    @Override
    public List<Vector2d> getVertices(
            final Transform transform,
            final List<Vector2d> result
    ) {
        val bounds = new RotatedRectangle(transform.position, this.origin, this.width, this.height, transform.rotation);
        result.add(bounds.getTopLeft(new Vector2d()));
        result.add(bounds.getTopRight(new Vector2d()));
        result.add(bounds.getBottomLeft(new Vector2d()));
        result.add(bounds.getBottomRight(new Vector2d()));

        return result;
    }

    public boolean isSolidTo(Entity entity, Collider collider) {
        return this.solid;
    }

    public boolean canOverlapsWith(Entity entity, Collider collider) {
        return this.overlaps;
    }

    public boolean canCollideWith(Entity entity, Collider collider) {
        return isSolidTo(entity, collider) || canOverlapsWith(entity, collider);
    }

    public Stream<Collision> getCollisions() {
        return this.collisions.stream()
                              .map(CollisionEvent::getCollision);
    }
}
