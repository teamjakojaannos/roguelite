package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
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
            LOG.debug("Reaped entity!");
            entityManager.destroyEntity(entity);
        });
    }
}
