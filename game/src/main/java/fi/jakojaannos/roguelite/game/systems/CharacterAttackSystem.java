package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.val;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CharacterAttackSystem implements ECSSystem<GameState> {
    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return List.of(Transform.class, CharacterInput.class, CharacterAbilities.class, CharacterStats.class);
    }

    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull GameState gameState,
            double delta,
            @NonNull Cluster cluster
    ) {
        entities.forEach(entity -> {
            val input = cluster.getComponentOf(entity, CharacterInput.class).get();
            val stats = cluster.getComponentOf(entity, CharacterStats.class).get();
            val abilities = cluster.getComponentOf(entity, CharacterAbilities.class).get();

            if (input.attack && abilities.attackTimer >= 1.0 / stats.attackRate) {
                val character = cluster.getComponentOf(entity, Transform.class).get();

                val projectile = cluster.createEntity();
                val projectileX = character.bounds.minX + character.getWidth() / 2.0;
                val projectileY = character.bounds.minY + character.getHeight() / 2.0;
                val transform = new Transform(projectileX, projectileY, 0.33, 0.33);
                cluster.addComponentTo(projectile, new ProjectileTag());
                cluster.addComponentTo(projectile, transform);

                val velocity = new Vector2d(abilities.attackTarget)
                                           .sub(projectileX, projectileY)
                                           .normalize(stats.attackProjectileSpeed);
                cluster.addComponentTo(projectile, new Velocity(velocity));
                abilities.attackTimer = 0.0;
            }
            abilities.attackTimer += delta;
        });
    }
}
