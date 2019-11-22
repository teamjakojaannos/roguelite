package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CharacterToTileMapCollisionSystem implements ECSSystem {
    private static final List<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            RecentCollisionTag.class,
            Transform.class,
            Collider.class
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
            val collider = world.getEntities().getComponentOf(entity, Collider.class).get();
            val transform = world.getEntities().getComponentOf(entity, Transform.class).get();

            if (!collider.tileCollisions.isEmpty()) {
                transform.setPosition(0, 0);
            }
        });
    }
}
