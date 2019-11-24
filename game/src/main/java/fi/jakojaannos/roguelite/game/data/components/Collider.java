package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.TileCollisionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Collider implements Component {
    public boolean solid;
    public final List<CollisionEvent> collisions = new ArrayList<>();

    public boolean isSolidTo(Entity entity, Collider collider) {
        return this.solid;
    }
}
