package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class PostUpdatePhysicsSystem implements ECSSystem {
    private static final List<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Physics.class,
            Transform.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    @Override
    public void tick(
            @NonNull final Stream<Entity> entities,
            @NonNull final World world,
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
