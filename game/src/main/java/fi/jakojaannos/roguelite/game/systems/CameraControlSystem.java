package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CameraControlSystem implements ECSSystem {
    @Override
    public void declareRequirements(@NonNull RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.LATE_TICK)
                    .withComponent(Camera.class);
    }

    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        entities.forEach(entity -> {
            val camera = world.getEntityManager().getComponentOf(entity, Camera.class).get();
            if (camera.followTarget != null) {
                world.getEntityManager()
                     .getComponentOf(camera.followTarget, Transform.class)
                     .ifPresent(transform -> camera.pos.set(transform.getCenterX(),
                                                            transform.getCenterY()));
            }
        });
    }
}
