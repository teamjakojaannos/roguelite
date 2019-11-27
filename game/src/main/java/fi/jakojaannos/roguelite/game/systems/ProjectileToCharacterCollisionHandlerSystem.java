package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.Collision;
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
            @NonNull final Stream<Entity> entities,
            @NonNull final World world,
            final double delta
    ) {
        val entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            val collider = entityManager.getComponentOf(entity, Collider.class).get();
            val stats = entityManager.getComponentOf(entity, ProjectileStats.class).get();

            val entityCollisions = collider.getCollisions()
                                           .filter(Collision::isEntity)
                                           .map(Collision::getAsEntityCollision);

            for (val collision : (Iterable<Collision.EntityCollision>) entityCollisions::iterator) {
                if (entityManager.hasComponent(collision.getOther(), Health.class)) {
                    val health = entityManager.getComponentOf(collision.getOther(), Health.class).get();
                    LOG.debug("Hit!");
                    health.currentHealth -= stats.damage;
                    entityManager.destroyEntity(entity);
                    // FIXME: Proper damage cool-down / invulnerability frame thingy
                    break;
                }
            }
        });
    }
}
