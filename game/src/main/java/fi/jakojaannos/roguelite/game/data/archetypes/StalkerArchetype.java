package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entities;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.val;

public class StalkerArchetype {

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
        val stalker = entities.createEntity();
        entities.addComponentTo(stalker, transform);
        entities.addComponentTo(stalker, new Velocity());
        entities.addComponentTo(stalker, new CharacterInput());
        entities.addComponentTo(stalker, createCharacterStats());
        entities.addComponentTo(stalker, createStalkerAi());

        return stalker;
    }


    private static CharacterStats createCharacterStats() {
        return new CharacterStats(
                1.0,
                100.0,
                800.0
        );
    }

    private static StalkerAI createStalkerAi() {
        return new StalkerAI(250.0f, 50.0f, 8.0f);
    }

}
