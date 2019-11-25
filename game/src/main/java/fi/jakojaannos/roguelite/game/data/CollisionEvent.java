package fi.jakojaannos.roguelite.game.data;

import lombok.Getter;
import lombok.NonNull;

public class CollisionEvent {
    @Getter private final Collision collision;

    public CollisionEvent(@NonNull final Collision collision) {
        this.collision = collision;
    }
}
