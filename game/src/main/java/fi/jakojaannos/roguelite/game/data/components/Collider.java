package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.utilities.math.RotatedRectangle;
import fi.jakojaannos.roguelite.game.systems.collision.Collision;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionEvent;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Defines entity collision boundaries and which other entities it should interact with. Each
 * collider belongs to a single {@link CollisionLayer}. The layer defines which other layers it
 * collides or overlaps with.
 * <p>
 * If a layer <strong>collides</strong> with another layer, all colliders belonging to the first
 * layer are blocked from collision boundaries of the second layer. Similarly, if layer is set to
 * <strong>overlap</strong> with another layer, it receives an overlap-event every time a collider
 * belonging to the other layer overlaps with its collision boundaries.
 * <p>
 * In other words: Each layer defines separately which other layers it treats as solid and from
 * which layers it wants overlap events.
 */
@Slf4j
public class Collider implements Component, Shape {
    public boolean solid = true;
    public boolean overlaps = true;
    public double width = 1.0;
    public double height = 1.0;
    public CollisionLayer layer;

    public Collider(CollisionLayer layer) {
        this.layer = layer;
    }

    public final Vector2d origin = new Vector2d();

    // TODO: Get rid of this, move to resources or sth. no reason to store these in individual
    //  components. Also, breaks everything if we move components to native memory later on.
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

    public boolean isSolidTo(final Collider collider) {
        return this.solid && this.layer.isSolidTo(collider.layer);
    }

    public boolean canOverlapWith(final Collider collider) {
        return this.overlaps && this.layer.canOverlapWith(collider.layer);
    }

    public boolean canCollideWith(final Collider collider) {
        return isSolidTo(collider) || canOverlapWith(collider);
    }

    public Stream<Collision> getCollisions() {
        return this.collisions.stream()
                              .map(CollisionEvent::getCollision);
    }
}
