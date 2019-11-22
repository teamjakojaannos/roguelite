package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import lombok.NonNull;

public class CollisionEvent implements Component {

    public final Entity entityA, entityB;

    public CollisionEvent(@NonNull Entity entityA, @NonNull Entity entityB) {
        this.entityA = entityA;
        this.entityB = entityB;
    }

}
