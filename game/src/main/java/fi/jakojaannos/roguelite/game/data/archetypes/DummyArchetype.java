package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entities;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Health;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.NonNull;
import lombok.val;

public class DummyArchetype {

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
        val dummy = entities.createEntity();
        entities.addComponentTo(dummy, transform);
        entities.addComponentTo(dummy, new Health(10));
        entities.addComponentTo(dummy, new Collider());

        return dummy;
    }

}
