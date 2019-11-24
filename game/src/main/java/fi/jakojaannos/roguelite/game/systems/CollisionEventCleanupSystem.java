package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.TileCollisionEvent;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.CollisionEvent;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Performs cleanup on all {@link Collider Colliders} to clear all unprocessed collision events at
 * the end of each tick. This includes both {@link CollisionEvent CollisionEvents} and {@link
 * TileCollisionEvent TileCollisionEvents}.
 *
 * @see SimpleColliderSystem
 * @see TileMapCollisionSystem
 */
public class CollisionEventCleanupSystem implements ECSSystem {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Collider.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        entities.forEach(entity -> {
            if (world.getEntities().hasComponent(entity, RecentCollisionTag.class)) {
                world.getEntities().removeComponentFrom(entity, RecentCollisionTag.class);
            }

            val collider = world.getEntities().getComponentOf(entity, Collider.class).get();
            collider.collisions.clear();
        });
    }
}
