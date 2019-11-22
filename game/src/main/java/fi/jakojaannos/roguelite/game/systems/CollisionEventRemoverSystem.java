package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.CollisionEvent;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CollisionEventRemoverSystem implements ECSSystem {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            RecentCollisionTag.class
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
            world.getEntities().removeComponentFrom(entity, RecentCollisionTag.class);
        });

    }
}
