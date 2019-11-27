package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.Collision;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.ProjectileStats;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class DestroyProjectilesOnCollisionSystem implements ECSSystem {
    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return List.of(Collider.class, ProjectileStats.class, RecentCollisionTag.class);
    }

    @Override
    public void tick(
            @NonNull final Stream<Entity> entities,
            @NonNull final World world,
            final double delta
    ) {
        entities.forEach(entity -> {
            val collider = world.getEntityManager().getComponentOf(entity, Collider.class).get();
            if (collider.getCollisions()
                        .anyMatch(c -> c.getMode() == Collision.Mode.COLLISION)) {
                world.getEntityManager().destroyEntity(entity);
            }
        });
    }
}
