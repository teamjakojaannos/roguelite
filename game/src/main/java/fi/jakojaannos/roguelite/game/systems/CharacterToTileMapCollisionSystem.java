package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.val;
import org.joml.Rectangled;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CharacterToTileMapCollisionSystem implements ECSSystem {
    private static final List<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            RecentCollisionTag.class,
            Transform.class,
            Collider.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    @Override
    public void tick(
            @NonNull final Stream<Entity> entities,
            @NonNull final World world,
            double delta
    ) {
        entities.forEach(entity -> {
            val collider = world.getEntities().getComponentOf(entity, Collider.class).get();
            val transform = world.getEntities().getComponentOf(entity, Transform.class).get();

            // TODO: This approach is not accurate for anything moving faster than 25 units/s
            //  -   If something can move faster than 0.5 units in a single tick, if they start
            //      moving just at the border of a tile, they will pass past the center-point,
            //      causing the push-out vector to point outwards, effectively giving the entity
            //      a small boost instead of pushing it back.
            //  -   We will need to trace the whole translation taking place during this frame
            //      and take extra cautionary measures to make sure no collisions occur
            //  -   Either iterate using step-based approach or do something smart like, stretching
            //      the collider towards the translation.

            // TODO: Detect collisions at corners and handle separately
            //  -   Currently, collision bounds of adjacent tiles are combined into one
            //  -   This works fine as long as this grows the combined collision bounds only on
            //      one axis (walking into a wall)
            //  -   When colliding with a corner, however, this causes the boundary to grow on
            //      both axes, making the collision area to seem larger than it really is
            //  -   This can be fixed by creating separate intersection rectangles for both axes
            //      and doing the push-out in two steps
            collider.tileCollisions.stream()
                                   .map(event -> event.getBounds(1.0, new Rectangled()))
                                   .filter(tileBounds -> tileBounds.intersects(transform.bounds))
                                   .reduce(this::combineRectangles)
                                   .map(combinedBounds -> intersection(transform.bounds, combinedBounds))
                                   .map(intersection -> findPushOutVector(transform.bounds, intersection))
                                   .ifPresent(transform.bounds::translate);
        });
    }

    @NonNull
    private Rectangled combineRectangles(Rectangled a, Rectangled b) {
        return new Rectangled(Math.min(a.minX, b.minX),
                              Math.min(a.minY, b.minY),
                              Math.max(a.maxX, b.maxX),
                              Math.max(a.maxY, b.maxY));
    }

    private Vector2d findPushOutVector(Rectangled bounds, Rectangled intersection) {
        val intersectionW = intersection.maxX - intersection.minX;
        val intersectionH = intersection.maxY - intersection.minY;

        double resultX, resultY;
        if (intersectionH < intersectionW) {
            resultX = 0.0;
            resultY = (Math.abs(intersection.maxY - bounds.maxY) > Math.abs(intersection.minY - bounds.minY))
                    ? intersectionH
                    : -intersectionH;
        } else {
            resultY = 0.0;
            resultX = (Math.abs(intersection.maxX - bounds.maxX) > Math.abs(intersection.minX - bounds.minX))
                    ? intersectionW
                    : -intersectionW;
        }

        return new Vector2d(resultX, resultY);
    }

    private Rectangled intersection(Rectangled a, Rectangled b) {
        return new Rectangled(Math.max(b.minX, a.minX),
                              Math.max(b.minY, a.minY),
                              Math.min(b.maxX, a.maxX),
                              Math.min(b.maxY, a.maxY));
    }
}
