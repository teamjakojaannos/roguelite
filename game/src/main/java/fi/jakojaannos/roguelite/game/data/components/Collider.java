package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.Collision;
import fi.jakojaannos.roguelite.game.data.CollisionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Collider implements Component {
    public boolean solid;
    public final List<CollisionEvent> collisions = new ArrayList<>();

    public boolean isSolidTo(Entity entity, Collider collider) {
        return this.solid;
    }

    public Stream<Collision> getCollisions() {
        return this.collisions.stream()
                              .map(CollisionEvent::getCollision);
    }
}
