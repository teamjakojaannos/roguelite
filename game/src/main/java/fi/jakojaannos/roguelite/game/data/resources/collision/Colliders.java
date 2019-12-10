package fi.jakojaannos.roguelite.game.data.resources.collision;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Colliders implements Resource {
    public final Map<CollisionLayer, List<ColliderEntity>> solidForLayer = new HashMap<>();
    public final Map<CollisionLayer, List<ColliderEntity>> overlapsWithLayer = new HashMap<>();

    @RequiredArgsConstructor
    public static final class ColliderEntity {
        public final Entity entity;
        public final Transform transform;
        public final Collider collider;
    }
}
