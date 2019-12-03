package fi.jakojaannos.roguelite.game.data;

import lombok.Getter;

public class CollisionEvent {
    @Getter private final Collision collision;

    public CollisionEvent( final Collision collision) {
        this.collision = collision;
    }
}
