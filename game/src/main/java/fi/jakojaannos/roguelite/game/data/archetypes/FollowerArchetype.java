package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entities;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.val;

public class FollowerArchetype {

    @NonNull
    public static Entity create(
            @NonNull final Entities entities,
            double x,
            double y
    ) {
        return create(
                entities,
                new Transform(x, y)
        );
    }

    @NonNull
    public static Entity create(
            @NonNull final Entities entities,
            @NonNull final Transform transform
    ) {
        val follower = entities.createEntity();
        entities.addComponentTo(follower, transform);
        entities.addComponentTo(follower, new Velocity());
        entities.addComponentTo(follower, new CharacterInput());
        entities.addComponentTo(follower, new Health(5));
        entities.addComponentTo(follower, new Collider());
        entities.addComponentTo(follower, createCharacterStats());
        entities.addComponentTo(follower, createEnemyAI());
        entities.addComponentTo(follower, createSpriteInfo());

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
