package fi.jakojaannos.roguelite.game.data.resources.collision;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Colliders implements Resource {
    public final Map<CollisionLayer, List<Entity>> solidForLayer = new HashMap<>();
    public final Map<CollisionLayer, List<Entity>> overlapsWithLayer = new HashMap<>();
}
