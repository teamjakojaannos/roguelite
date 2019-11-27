package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.val;

public class FollowerArchetype {

    @NonNull
    public static Entity create(
            @NonNull final EntityManager entityManager,
            double x,
            double y
    ) {
        return create(
                entityManager,
                new Transform(x, y)
        );
    }

    @NonNull
    public static Entity create(
            @NonNull final EntityManager entityManager,
            @NonNull final Transform transform
    ) {
        val follower = entityManager.createEntity();
        entityManager.addComponentTo(follower, transform);
        entityManager.addComponentTo(follower, new Velocity());
        entityManager.addComponentTo(follower, new CharacterInput());
        entityManager.addComponentTo(follower, new Health(5));
        entityManager.addComponentTo(follower, new Collider());
        entityManager.addComponentTo(follower, createCharacterStats());
        entityManager.addComponentTo(follower, createEnemyAI());
        entityManager.addComponentTo(follower, createSpriteInfo());

        return follower;
    }


    private static CharacterStats createCharacterStats() {
        return new CharacterStats(
                4.0,
                100.0,
                800.0
        );
    }

    private static FollowerEnemyAI createEnemyAI() {
        return new FollowerEnemyAI(25.0f, 1.0f);
    }


    private static SpriteInfo createSpriteInfo() {
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/enemy";

        return sprite;
    }
}
