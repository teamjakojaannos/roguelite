package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.CharacterStats;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CharacterToTileMapCollisionHandlerSystem implements ECSSystem {
    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return List.of(Transform.class, Collider.class, CharacterStats.class);
    }

    @Override
    public void tick(
            @NonNull final Stream<Entity> entities,
            @NonNull final World world,
            final double delta
    ) {

    }
}
