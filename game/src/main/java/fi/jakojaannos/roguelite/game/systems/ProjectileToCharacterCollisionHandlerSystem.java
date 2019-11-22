package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class ProjectileToCharacterCollisionHandlerSystem implements ECSSystem {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Collider.class, RecentCollisionTag.class, ProjectileTag.class
    );

    private static final List<Class<? extends Resource>> REQUIRED_RESOURCES = List.of(
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents()
    {
        return REQUIRED_COMPONENTS;
    }

    @Override
    public Collection<Class<? extends Resource>> getRequiredResources()
    {
        return REQUIRED_RESOURCES;
    }


    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta)
    {


        entities.forEach(entity -> {

            var collider = world.getEntities().getComponentOf(entity, Collider.class).get();

            for (val event : collider.collisions) {
                if (world.getEntities().hasComponent(event.other, Health.class)) {
                    LOG.debug("Hit!");
                    world.getEntities().destroyEntity(entity);
                    world.getEntities().destroyEntity(event.other);
                }
            }


        });

    }
}
