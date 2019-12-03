package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.val;

public class ObstacleArchetype {
    public static Entity create(
             final EntityManager entityManager,
             final Transform transform
    ) {
        val obstacle = entityManager.createEntity();
        entityManager.addComponentTo(obstacle, transform);
        entityManager.addComponentTo(obstacle, createCollider());
        entityManager.addComponentTo(obstacle, createSpriteInfo());
        return obstacle;
    }

    private static Collider createCollider() {
        val collider = new Collider();
        collider.solid = true;
        return collider;
    }

    private static SpriteInfo createSpriteInfo() {
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/obstacle";

        return sprite;
    }
}
