package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.CollisionEvent;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Performs cleanup on all {@link Collider Colliders} to clear all unprocessed {@link CollisionEvent
 * CollisionEvents} at the end of each tick.
 *
 * @see ApplyVelocitySystem
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
            world.getEntityManager().removeComponentIfPresent(entity, RecentCollisionTag.class);

            world.getEntityManager()
                 .getComponentOf(entity, Collider.class)
                 .get().collisions.clear();
        });
    }
}
