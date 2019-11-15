package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.CharacterStats;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class CharacterMovementSystem implements ECSSystem<GameState> {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Transform.class, Velocity.class, CharacterInput.class, CharacterStats.class
    );
    private static final float INPUT_EPSILON = 0.001f;

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    private final Vector2d tmpVelocity = new Vector2d();

    @Override
    public void tick(
            Stream<Entity> entities,
            GameState state,
            double delta,
            Cluster cluster
    ) {
        entities.forEach(entity -> {
            val input = cluster.getComponentOf(entity, CharacterInput.class).get();
            val stats = cluster.getComponentOf(entity, CharacterStats.class).get();
            val velocity = cluster.getComponentOf(entity, Velocity.class).get();

            // Accelerate
            if (input.move.lengthSquared() > INPUT_EPSILON * INPUT_EPSILON) {
                input.move.normalize(stats.acceleration * delta, tmpVelocity);
                tmpVelocity.add(velocity.velocity);

                if (tmpVelocity.lengthSquared() > stats.speed * stats.speed) {
                    tmpVelocity.normalize(stats.speed);
                }
                velocity.velocity.set(tmpVelocity);
            }
            // Deceleration
            else {
                val decelerationThisFrame = stats.friction * (float) delta;
                val xVel = velocity.velocity.x;
                val yVel = velocity.velocity.y;
                velocity.velocity.set(Math.signum(xVel) * Math.max(0.0f, Math.abs(xVel) - decelerationThisFrame),
                                      Math.signum(yVel) * Math.max(0.0f, Math.abs(yVel) - decelerationThisFrame));
            }

            val transform = cluster.getComponentOf(entity, Transform.class).get();
            velocity.velocity.mul(delta, tmpVelocity);
            transform.bounds.translate(tmpVelocity);
        });
    }
}
