package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Health;
import fi.jakojaannos.roguelite.game.data.components.ProjectileStats;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class ProjectileToCharacterCollisionHandlerSystem implements ECSSystem {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Collider.class, RecentCollisionTag.class, ProjectileStats.class
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

        val cluster = world.getEntities();

        entities.forEach(entity -> {

            val collider = cluster.getComponentOf(entity, Collider.class).get();
            val stats = cluster.getComponentOf(entity, ProjectileStats.class).get();

            for (val event : collider.collisions) {
                if (cluster.hasComponent(event.other, Health.class)) {

                    val hp = cluster.getComponentOf(event.other, Health.class).get();
                    LOG.debug("Hit!");
                    hp.currentHealth -= stats.damage;
                    cluster.destroyEntity(entity);
                }
            }


        });

    }
}
