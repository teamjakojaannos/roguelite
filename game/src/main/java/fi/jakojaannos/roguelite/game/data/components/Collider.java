package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.game.data.TileCollisionEvent;

import java.util.ArrayList;
import java.util.List;

public class Collider implements Component {
    public final List<CollisionEvent> collisions = new ArrayList<>();
    public final List<TileCollisionEvent> tileCollisions = new ArrayList<>();
}
