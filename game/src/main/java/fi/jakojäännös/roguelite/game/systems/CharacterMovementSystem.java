package fi.jakojäännös.roguelite.game.systems;

import fi.jakojäännös.roguelite.engine.ecs.Cluster;
import fi.jakojäännös.roguelite.engine.ecs.Component;
import fi.jakojäännös.roguelite.engine.ecs.ECSSystem;
import fi.jakojäännös.roguelite.engine.ecs.Entity;
import fi.jakojäännös.roguelite.game.data.GameState;
import fi.jakojäännös.roguelite.game.data.components.CharacterInput;
import fi.jakojäännös.roguelite.game.data.components.CharacterStats;
import fi.jakojäännös.roguelite.game.data.components.Position;
import fi.jakojäännös.roguelite.game.data.components.Velocity;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2f;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class CharacterMovementSystem implements ECSSystem<GameState> {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Position.class, Velocity.class, CharacterInput.class, CharacterStats.class
    );
    private static final float INPUT_EPSILON = 0.00001f;

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

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
            if (input.move.lengthSquared() > INPUT_EPSILON) {
                val accelerationThisFrame = stats.acceleration * (float) delta;
                val accelerationInput = input.move.mul(accelerationThisFrame, new Vector2f());

                val magnitude = velocity.velocity.add(accelerationInput)
                                                 .length();
                if (magnitude > INPUT_EPSILON) {
                    velocity.velocity.normalize(Math.min(magnitude, stats.speed));
                } else {
                    velocity.velocity.set(0.0f, 0.0f);
                }
            }
            // Deceleration
            else {
                val decelerationThisFrame = stats.friction * (float) delta;
                val xVel = velocity.velocity.x;
                val yVel = velocity.velocity.y;
                velocity.velocity.set(Math.signum(xVel) * Math.max(0.0f, Math.abs(xVel) - decelerationThisFrame),
                                      Math.signum(yVel) * Math.max(0.0f, Math.abs(yVel) - decelerationThisFrame));
            }

            val position = cluster.getComponentOf(entity, Position.class).get();
            position.x += velocity.velocity.x * delta;
            position.y += velocity.velocity.y * delta;
        });
    }
}
