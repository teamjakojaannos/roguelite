package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.engine.utilities.GenerateStream;
import fi.jakojaannos.roguelite.game.data.Collision;
import fi.jakojaannos.roguelite.game.data.CollisionEvent;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Rectangled;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ApplyVelocitySystem implements ECSSystem {
    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return List.of(Transform.class, Velocity.class);
    }

    private final Vector2d tmpVelocity = new Vector2d();
    private final Rectangled tmpTargetBounds = new Rectangled();

    @Override
    public void tick(
            @NonNull final Stream<Entity> entities,
            @NonNull final World world,
            final double delta
    ) {
        val tileMapLayers = world.getEntities()
                                 .getEntitiesWith(TileMapLayer.class)
                                 .map(Entities.EntityComponentPair::getComponent)
                                 .filter(TileMapLayer::isCollisionEnabled)
                                 .map(TileMapLayer::getTileMap)
                                 .collect(Collectors.toList());

        entities.forEach(entity -> {
            val transform = world.getEntities().getComponentOf(entity, Transform.class).get();
            val velocity = world.getEntities().getComponentOf(entity, Velocity.class).get();

            if (velocity.velocity.length() < 0.001) {
                return;
            }

            final Rectangled targetBounds;
            if (world.getEntities().hasComponent(entity, Collider.class)) {
                targetBounds = updateTargetBoundsWithCollisionDetection(delta, tileMapLayers, transform, velocity.velocity);
            } else {
                velocity.velocity.mul(delta, tmpVelocity);
                targetBounds = transform.bounds.translate(tmpVelocity, new Rectangled());
            }

            applyBounds(targetBounds, transform.bounds);
        });
    }

    private Rectangled updateTargetBoundsWithCollisionDetection(
            final double delta,
            @NonNull final List<TileMap<TileType>> tileMapLayers,
            @NonNull final Transform transform,
            @NonNull final Vector2d velocityRaw
    ) {
        val direction = velocityRaw.normalize(new Vector2d());
        val maxDistance = velocityRaw.length() * delta;

        val initialBounds = transform.bounds;
        val stepSize = 0.01;

        val targetBounds = new Rectangled();
        var distanceRemaining = maxDistance;
        val currentBounds = new Rectangled(initialBounds);
        while (distanceRemaining > 0.0) {
            val distanceMoved = tryMove(tileMapLayers, direction, stepSize, currentBounds, targetBounds);

            if (distanceMoved < 0.0) {
                break;
            }
            applyBounds(targetBounds, currentBounds);
            distanceRemaining -= distanceMoved;
        }

        return targetBounds;
    }

    private double tryMove(
            @NonNull List<TileMap<TileType>> tileMapLayers,
            Vector2d direction,
            double distance,
            Rectangled initialBounds,
            Rectangled outTargetBounds
    ) {
        val distanceMoved = checkForCollisionsInDirection(distance, direction, tileMapLayers, initialBounds, outTargetBounds);
        if (distanceMoved >= 0.0) {
            return distanceMoved;
        }

        val directionX = new Vector2d(direction.x, 0.0);
        val targetBoundsX = new Rectangled();
        val distanceX = checkForCollisionsInDirection(distance, directionX, tileMapLayers, initialBounds, targetBoundsX);

        val directionY = new Vector2d(0.0, direction.y);
        val targetBoundsY = new Rectangled();
        val distanceY = checkForCollisionsInDirection(distance, directionY, tileMapLayers, initialBounds, targetBoundsY);

        // Both directions succeeded, move on the axis we can travel furthest
        if (distanceX > 0 && distanceY > 0) {
            if (distanceX > distanceY) {
                applyBounds(targetBoundsX, outTargetBounds);
                return distanceX;
            } else {
                applyBounds(targetBoundsY, outTargetBounds);
                return distanceY;
            }
        }
        // Only X succeeded
        else if (distanceX > 0) {
            applyBounds(targetBoundsX,
                        outTargetBounds);
            return distanceX;
        }
        // Only Y succeeded
        else if (distanceY > 0) {
            applyBounds(targetBoundsY,
                        outTargetBounds);
            return distanceY;
        }
        // Neither succeeded
        else {
            applyBounds(initialBounds,
                        outTargetBounds);
            return -1.0;
        }
    }

    private double checkForCollisionsInDirection(
            final double distance,
            @NonNull final Vector2d direction,
            @NonNull final List<TileMap<TileType>> tileMapLayers,
            @NonNull final Rectangled initialBounds,
            @NonNull final Rectangled outTargetBounds
    ) {
        val targetBounds = initialBounds.translate(direction.mul(distance, new Vector2d()), new Rectangled());
        val collisions = getCollidingTiles(tileMapLayers, initialBounds, targetBounds);

        if (!collisions.isEmpty()) {
            applyBounds(initialBounds, outTargetBounds);
            return -1.0;
        }

        applyBounds(targetBounds, outTargetBounds);
        return distance;
    }

    private void applyBounds(Rectangled from, Rectangled to) {
        to.minX = from.minX;
        to.minY = from.minY;
        to.maxX = from.maxX;
        to.maxY = from.maxY;
    }

    private ArrayList<Rectangled> getCollidingTiles(
            List<TileMap<TileType>> tileMapLayers,
            Rectangled initialBounds,
            Rectangled targetBounds
    ) {
        val collisions = new ArrayList<Rectangled>();
        val startX = Math.min((int) Math.floor(initialBounds.minX), (int) Math.floor(targetBounds.minX));
        val startY = Math.min((int) Math.floor(initialBounds.minY), (int) Math.floor(targetBounds.minY));
        val endX = Math.max((int) Math.ceil(initialBounds.maxX), (int) Math.ceil(targetBounds.maxX));
        val endY = Math.max((int) Math.ceil(initialBounds.maxY), (int) Math.ceil(targetBounds.maxY));

        val width = endX - startX;
        val height = endY - startY;

        GenerateStream.ofCoordinates(startX, startY, width, height)
                      .filter(pos -> tileMapLayers.stream()
                                                  .map(tm -> tm.getTile(pos))
                                                  .anyMatch(TileType::isSolid))
                      .map(pos -> new Rectangled(pos.x, pos.y, pos.x + 1, pos.y + 1))
                      // TODO: Stretched collider instead of translated AABB
                      .filter(targetBounds::intersects)
                      .forEach(collisions::add);
        return collisions;
    }

    private void gatherPossiblyCollidingEntities(
            @NonNull final World world,
            @NonNull final List<Entity> entitiesWithCollider,
            @NonNull final Entity entity,
            @NonNull final Transform transform,
            @NonNull final Collider collider,
            @NonNull final List<CollisionCandidate> collisions,
            @NonNull final List<CollisionCandidate> nonSolidCollisionCandidates
    ) {
        for (val other : entitiesWithCollider) {
            if (other.getId() == entity.getId()) {
                continue;
            }

            val otherCollider = world.getEntities().getComponentOf(other, Collider.class).get();
            val otherTransform = world.getEntities().getComponentOf(other, Transform.class).get();

            val intersects = otherTransform.bounds.intersects(transform.bounds);
            if (intersects) {
                val candidate = new CollisionCandidate(other, otherTransform.bounds, otherCollider);
                if (otherCollider.isSolidTo(entity, collider)) {
                    collisions.add(candidate);
                } else {
                    nonSolidCollisionCandidates.add(candidate);
                }
            }
        }
    }

    private void fireCollisionEvent(
            @NonNull final World world,
            @NonNull final Entity entity,
            @NonNull final Collider collider,
            @NonNull final Collision collision
    ) {
        collider.collisions.add(new CollisionEvent(collision));

        if (!world.getEntities().hasComponent(entity, RecentCollisionTag.class)) {
            world.getEntities().addComponentTo(entity, new RecentCollisionTag());
        }
    }


    @RequiredArgsConstructor
    private static class CollisionCandidate {
        @Getter private final Entity other;
        @Getter private final Rectangled otherBounds;
        @Getter private final Collider otherCollider;

        public boolean isTile() {
            return this.other == null || this.otherCollider == null;
        }

        public CollisionCandidate(Rectangled otherBounds) {
            this.otherBounds = otherBounds;
            this.other = null;
            this.otherCollider = null;
        }
    }
}
