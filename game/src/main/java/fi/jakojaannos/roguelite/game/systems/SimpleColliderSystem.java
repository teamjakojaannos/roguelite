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
            Collider.class, Transform.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }


    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        var entityList = entities.collect(Collectors.toList());
        val cluster = world.getEntities();

        for (val entityA : entityList) {
            val posA = cluster.getComponentOf(entityA, Transform.class).get();
            val collider = cluster.getComponentOf(entityA, Collider.class).get();

            for (val entityB : entityList) {
                if (entityA.getId() == entityB.getId()) continue;

                val posB = cluster.getComponentOf(entityB, Transform.class).get();


                if (posA.bounds.intersects(posB.bounds)) {
                    collider.collisions.add(new CollisionEvent(entityB));
                    if (!cluster.hasComponent(entityA, RecentCollisionTag.class))
                        cluster.addComponentTo(entityA, new RecentCollisionTag());

                }


            }
        }

    }

}
