package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class HealthCheckSystem implements ECSSystem {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Health.class
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
        val cluster = world.getEntityManager();

        entities.forEach(entity -> {

            val hp = cluster.getComponentOf(entity, Health.class).get();
            if (hp.currentHealth <= 0.0f) {
                LOG.debug("Dead");
                cluster.destroyEntity(entity);
            }

        });
    }
}
