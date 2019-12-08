package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.Health;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j

public class HealthUpdateSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.LATE_TICK)
                .withComponent(Health.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world,
            final double delta
    ) {
        val entityManager = world.getEntityManager();

        entities.forEach(entity -> {
            val hp = entityManager.getComponentOf(entity, Health.class).get();

            val dmgList = hp.damageInstances;
            for (val dmg : dmgList) {
                LOG.debug("Oof");
                hp.currentHealth -= dmg.damage;
            }

            dmgList.clear();

            if (hp.currentHealth <= 0.0f) {
                LOG.debug("Dead");
                entityManager.addComponentIfAbsent(entity, new DeadTag());
                // FIXME: Move this somewhere else once Reaper is merged to master
                if (world.getResource(Players.class).player != null && entity.getId() == world.getResource(Players.class).player.getId()) {
                    world.getResource(Players.class).player = null;
                    entityManager.getEntitiesWith(Camera.class)
                            .map(EntityManager.EntityComponentPair::getComponent)
                            .filter(camera -> camera.followTarget == entity)
                            .forEach(camera -> camera.followTarget = null);
                }
            }
        });
    }
}

