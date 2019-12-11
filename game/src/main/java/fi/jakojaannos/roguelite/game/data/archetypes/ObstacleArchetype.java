package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.val;

public class ObstacleArchetype {
    public static Entity create(
            final EntityManager entityManager,
            final Transform transform,
            final double size
    ) {
        val obstacle = entityManager.createEntity();
        entityManager.addComponentTo(obstacle, transform);
        entityManager.addComponentTo(obstacle, createCollider(size));
        entityManager.addComponentTo(obstacle, createSpriteInfo());
        return obstacle;
    }

    private static Collider createCollider(final double size) {
        val collider = new Collider(CollisionLayer.OBSTACLE);
        collider.width = size;
        collider.height = size;
        return collider;
    }

    private static SpriteInfo createSpriteInfo() {
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/obstacle";

        return sprite;
    }
}
