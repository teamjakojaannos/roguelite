package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.LogCategories;
import fi.jakojaannos.roguelite.game.data.components.DeadTag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j
public class ReaperSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .withComponent(DeadTag.class);
    }

    @Override
    public void tick(
            Stream<Entity> entities,
            World world,
            double delta
    ) {
        val entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            entityManager.destroyEntity(entity);
            LOG.debug(LogCategories.DEATH, "Destroyed a dead entity {}", entity.getId());
        });
    }
}
