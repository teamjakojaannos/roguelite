package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.val;

import java.util.stream.Stream;

public class CameraControlSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.LATE_TICK)
                    .withComponent(Camera.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            val camera = entityManager.getComponentOf(entity, Camera.class)
                                      .orElseThrow();
            if (camera.followTarget == null || camera.followTarget.isMarkedForRemoval()) {
                camera.followTarget = null;
                return;
            }

            entityManager.getComponentOf(camera.followTarget, Transform.class)
                         .ifPresent(transform -> camera.pos.set(transform.position));
        });
    }
}
