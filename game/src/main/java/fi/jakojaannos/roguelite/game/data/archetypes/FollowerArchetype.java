package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import lombok.val;

public class FollowerArchetype {
    public static Entity spawnFollower(
            final EntityManager entityManager,
            final Transform spawnerTransform,
            final SpawnerComponent spawnerComponent
    ) {
        return create(entityManager, new Transform(spawnerTransform));
    }


    public static Entity create(
            final EntityManager entityManager,
            final Transform transform
    ) {
        val follower = entityManager.createEntity();
        entityManager.addComponentTo(follower, transform);
        entityManager.addComponentTo(follower, new Velocity());
        entityManager.addComponentTo(follower, new CharacterInput());
        entityManager.addComponentTo(follower, new Health(5));
        entityManager.addComponentTo(follower, new Collider(CollisionLayer.ENEMY, 1.0, 1.0, 0.5, 0.5));
        entityManager.addComponentTo(follower, createCharacterStats());
        entityManager.addComponentTo(follower, createEnemyAI());
        entityManager.addComponentTo(follower, createSpriteInfo());
        entityManager.addComponentTo(follower, new EnemyTag());
        entityManager.addComponentTo(follower, new CharacterAbilities());
        entityManager.addComponentTo(follower, new EnemyMeleeWeaponStats());

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
