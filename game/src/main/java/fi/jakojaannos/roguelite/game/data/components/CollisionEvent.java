package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import lombok.NonNull;

public class CollisionEvent implements Component {

    public final Entity other;

    public CollisionEvent(@NonNull Entity other) {
        this.other = other;
    }

}
