package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Health;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j
public class HealthCheckSystem implements ECSSystem {
    @Override
    public void declareRequirements( RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.LATE_TICK)
                    .withComponent(Health.class);
    }

    @Override
    public void tick(
             Stream<Entity> entities,
             World world,
            double delta
    ) {
        val cluster = world.getEntityManager();

        entities.forEach(entity -> {

            val hp = cluster.getComponentOf(entity, Health.class).get();
            if (hp.currentHealth <= 0.0f) {
                LOG.debug("Dead");
                cluster.destroyEntity(entity);
            }

        });
    }
}
