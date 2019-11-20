package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.ProjectileTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import lombok.NonNull;
import lombok.val;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ProjectileMovementSystem implements ECSSystem {
    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return List.of(ProjectileTag.class, Transform.class, Velocity.class);
    }

    private final Vector2d tmpVelocity = new Vector2d();

    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        entities.forEach(entity -> {
            val transform = world.getEntities().getComponentOf(entity, Transform.class).get();
            val velocity = world.getEntities().getComponentOf(entity, Velocity.class).get();
            transform.bounds.translate(velocity.velocity.mul(delta, tmpVelocity));
        });

    }
}
