package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entities;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.val;

public class ObstacleArchetype {
    public static Entity create(
            @NonNull final Entities entities,
            @NonNull final Transform transform
    ) {
        val obstacle = entities.createEntity();
        entities.addComponentTo(obstacle, transform);
        entities.addComponentTo(obstacle, createCollider());
        entities.addComponentTo(obstacle, createSpriteInfo());
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
