package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SimpleColliderSystem implements ECSSystem {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            ColliderTag.class, Transform.class
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
        var entityList = entities.collect(Collectors.toList());

        val cluster = world.getEntities();

        for (val entityA : entityList) {
            val posA = cluster.getComponentOf(entityA, Transform.class).get();

            for (val entityB : entityList) {
                if (entityA.getId() == entityB.getId()) continue;

                val posB = cluster.getComponentOf(entityB, Transform.class).get();

                if (posA.bounds.intersects(posB.bounds)) {
                    cluster.addComponentTo(entityA, new CollisionEvent(entityA, entityB));
                    cluster.addComponentTo(entityB, new CollisionEvent(entityB, entityA));
                }


            }
        }

    }

}
