package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class PostUpdatePhysicsSystem implements ECSSystem {
    @Override
    public void declareRequirements( RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.LATE_TICK)
                    .withComponent(Physics.class)
                    .withComponent(Transform.class);
    }

    @Override
    public void tick(
             final Stream<Entity> entities,
             final World world,
            double delta
    ) {
        entities.forEach(entity -> {
            val physics = world.getEntityManager().getComponentOf(entity, Physics.class).get();
            val transform = world.getEntityManager().getComponentOf(entity, Transform.class).get();

            physics.oldBounds.minX = transform.bounds.minX;
            physics.oldBounds.minY = transform.bounds.minY;
            physics.oldBounds.maxX = transform.bounds.maxX;
            physics.oldBounds.maxY = transform.bounds.maxY;
        });
    }
}
