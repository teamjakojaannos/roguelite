package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.collision.Collision;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.ProjectileStats;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j
public class DestroyProjectilesOnCollisionSystem implements ECSSystem {
    @Override
    public void declareRequirements( RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.COLLISION_HANDLER)
                    .tickAfter(ProjectileToCharacterCollisionHandlerSystem.class)
                    .withComponent(Collider.class)
                    .withComponent(ProjectileStats.class)
                    .withComponent(RecentCollisionTag.class);
    }

    @Override
    public void tick(
             final Stream<Entity> entities,
             final World world,
            final double delta
    ) {
        entities.forEach(entity -> {
            val collider = world.getEntityManager().getComponentOf(entity, Collider.class).get();
            if (collider.getCollisions()
                        .anyMatch(c -> c.getMode() == Collision.Mode.COLLISION)) {
                world.getEntityManager().destroyEntity(entity);
            }
        });
    }
}
