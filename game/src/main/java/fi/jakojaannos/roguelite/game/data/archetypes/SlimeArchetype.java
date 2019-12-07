package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.val;

public class SlimeArchetype {


    public static Entity create(
            final EntityManager entityManager,
            double x,
            double y
    ) {
        val slime = entityManager.createEntity();
        entityManager.addComponentTo(slime, new Transform(x, y));
        entityManager.addComponentTo(slime, new SlimeAI());
        entityManager.addComponentTo(slime, new Velocity());
        entityManager.addComponentTo(slime, new CharacterInput());
        entityManager.addComponentTo(slime, createCharacterStats());

        return slime;

    }


    private static CharacterStats createCharacterStats() {
        return new CharacterStats(
                4.5,
                100.0,
                800.0
        );
    }

}
