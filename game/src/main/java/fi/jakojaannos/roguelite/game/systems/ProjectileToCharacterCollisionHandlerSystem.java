package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.CollisionEvent;
import fi.jakojaannos.roguelite.game.data.components.ProjectileTag;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class ProjectileToCharacterCollisionHandlerSystem implements ECSSystem {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            CollisionEvent.class
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

            var event = world.getEntities().getComponentOf(entity, CollisionEvent.class).get();

            var entityA = event.entityA;
            var entityB = event.entityB;

            if(world.getEntities().getComponentOf(entityB, ProjectileTag.class).isPresent()){
                LOG.debug("Hit!");
            }


        });

    }
}
