package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import lombok.val;
import org.joml.Vector2d;

public class SlimeArchetype {

    public static Entity createSmallSlimeWithInitialVelocity(
            final EntityManager entityManager,
            double x,
            double y,
            Vector2d dir,
            double airTime
    ) {
        val slime = entityManager.createEntity();
        entityManager.addComponentTo(slime, new Transform(x, y));
        entityManager.addComponentTo(slime, new Health(3));
        entityManager.addComponentTo(slime, new Collider(CollisionLayer.ENEMY, 0.6));
        entityManager.addComponentTo(slime, new Velocity());
        entityManager.addComponentTo(slime, new CharacterInput());

        val slimeAi = new SlimeAI(
                0.2,
                0.4,
                1
        );
        slimeAi.airTime = airTime;
        slimeAi.jumpCoolDown = airTime * 1.2;
        slimeAi.jumpDir.set(dir);

        entityManager.addComponentTo(slime, slimeAi);
        entityManager.addComponentTo(slime, new CharacterStats(
                11.0,
                100.0,
                800.0
        ));
        entityManager.addComponentTo(slime, createSpriteInfo());


        return slime;
    }


    public static Entity createMediumSlimeWithInitialVelocity(
            final EntityManager entityManager,
            double x,
            double y,
            Vector2d dir,
            double airTime
    ) {
        val slime = entityManager.createEntity();
        entityManager.addComponentTo(slime, new Transform(x, y));
        entityManager.addComponentTo(slime, new Health(7));
        entityManager.addComponentTo(slime, new Collider(CollisionLayer.ENEMY, 1.0));
        entityManager.addComponentTo(slime, new Velocity());
        entityManager.addComponentTo(slime, new CharacterInput());

        val slimeAi = new SlimeAI(
                0.3,
                0.8,
                2
        );
        slimeAi.airTime = airTime;
        slimeAi.jumpCoolDown = airTime * 1.2;
        slimeAi.jumpDir.set(dir);

        entityManager.addComponentTo(slime, slimeAi);
        entityManager.addComponentTo(slime, new CharacterStats(
                6.0,
                100.0,
                800.0
        ));
        entityManager.addComponentTo(slime, createSpriteInfo());


        return slime;
    }

    public static Entity createMediumSlime(
            final EntityManager entityManager,
            double x,
            double y
    ) {
        val slime = entityManager.createEntity();
        entityManager.addComponentTo(slime, new Transform(x, y));
        entityManager.addComponentTo(slime, new Health(7));
        entityManager.addComponentTo(slime, new Collider(CollisionLayer.ENEMY, 1.0));
        entityManager.addComponentTo(slime, new Velocity());
        entityManager.addComponentTo(slime, new CharacterInput());
        entityManager.addComponentTo(slime, new SlimeAI(
                0.3,
                0.8,
                2
        ));
        entityManager.addComponentTo(slime, new CharacterStats(
                6.0,
                100.0,
                800.0
        ));
        entityManager.addComponentTo(slime, createSpriteInfo());


        return slime;
    }


    public static Entity createLargeSlime(
            final EntityManager entityManager,
            double x,
            double y
    ) {
        val slime = entityManager.createEntity();
        entityManager.addComponentTo(slime, new Transform(x, y));
        entityManager.addComponentTo(slime, new Health(15));
        entityManager.addComponentTo(slime, new Collider(CollisionLayer.ENEMY, 1.75));
        entityManager.addComponentTo(slime, new Velocity());
        entityManager.addComponentTo(slime, new CharacterInput());
        entityManager.addComponentTo(slime, new SlimeAI(
                0.6,
                1.0,
                3
        ));
        entityManager.addComponentTo(slime, new CharacterStats(
                4.5,
                100.0,
                800.0
        ));
        entityManager.addComponentTo(slime, createSpriteInfo());


        return slime;
    }

    private static SpriteInfo createSpriteInfo() {
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/slime";

        return sprite;
    }


}
