package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.val;
import org.joml.Vector2d;

public class BasicProjectileArchetype {

    @NonNull
    public static Entity create(
            @NonNull final World world,
            double projectileX,
            double projectileY,
            @NonNull final Vector2d direction,
            final double projectileSpeed,
            @NonNull Vector2d spreadOffset
    ) {
        return create(
                world,
                createTransform(projectileX, projectileY),
                new Velocity(direction.normalize(projectileSpeed)
                                      .add(spreadOffset)
                ));
    }

    @NonNull
    public static Entity create(
            @NonNull final World world,
            double projectileX,
            double projectileY,
            @NonNull final Vector2d direction,
            final double projectileSpeed
    ) {

        return create(
                world,
                createTransform(projectileX, projectileY),
                new Velocity(direction.normalize(projectileSpeed)
                ));
    }

    @NonNull
    public static Entity create(
            @NonNull final World world,
            @NonNull final Transform transform,
            @NonNull final Velocity velocity
    ) {
        val entities = world.getEntityManager();

        val projectile = entities.createEntity();
        val projectileStats = createProjectileStats();

        entities.addComponentTo(projectile, projectileStats);
        entities.addComponentTo(projectile, new Physics(transform));
        entities.addComponentTo(projectile, new Collider());
        entities.addComponentTo(projectile, transform);
        entities.addComponentTo(projectile, velocity);
        entities.addComponentTo(projectile, createSpriteInfo());

        return projectile;
    }

    private static ProjectileStats createProjectileStats() {
        return new ProjectileStats(1.0);
    }

    private static Transform createTransform(double x, double y) {
        return new Transform(
                x,
                y,
                0.3,
                0.3,
                0.15,
                0.15);
    }


    private static SpriteInfo createSpriteInfo() {
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/projectile";

        return sprite;
    }
}
