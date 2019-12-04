package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.collision.CollisionEvent;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;

import java.util.stream.Stream;

/**
 * Performs cleanup on all {@link Collider Colliders} to clear all unprocessed {@link CollisionEvent
 * CollisionEvents} at the end of each tick.
 *
 * @see ApplyVelocitySystem
 */
public class CollisionEventCleanupSystem implements ECSSystem {
    @Override
    public void declareRequirements( RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .withComponent(Collider.class);
    }

    @Override
    public void tick(
             Stream<Entity> entities,
             World world,
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
