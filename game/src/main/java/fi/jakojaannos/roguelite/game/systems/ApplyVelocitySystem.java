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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Applies velocity read from the {@link Velocity} component to character {@link Transform},
 * handling collisions and firing {@link CollisionEvent Collision Events} whenever necessary.
 * Backbone of the physics and collision detection of characters and other simple moving entities.
 *
 * @implNote Current implementation utilizes <code>O(n^2)</code> complexity, considering all
 * entities with a collider to be viable for collisions. Additionally, as implementation uses sort
 * of a "approach-carefully" -style logic when nearing collision, this might result in a very high
 * number of iterations required for fast-moving entities. This implementation is to be considered
 * WIP and better one is currently medium priority TODO
 * @see CollisionEvent
 * @see Collision
 */
@Slf4j
public class ApplyVelocitySystem implements ECSSystem {
    @Override
    public void declareRequirements(@NonNull RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.PHYSICS_TICK)
                    .withComponent(Transform.class)
                    .withComponent(Velocity.class);
    }

    private final Vector2d tmpVelocity = new Vector2d();

    @Override
    public void tick(
            @NonNull final Stream<Entity> entities,
            @NonNull final World world,
            final double delta
    ) {
        val entitiesWithCollider = world.getEntityManager()
                                        .getEntitiesWith(List.of(Transform.class, Collider.class))
                                        .collect(Collectors.toUnmodifiableList());

        val tileMapLayers = getTileMapLayersWithCollision(world);

        entities.forEach(entity -> {
            val transform = world.getEntityManager().getComponentOf(entity, Transform.class).get();
            val velocity = world.getEntityManager().getComponentOf(entity, Velocity.class).get();

            if (velocity.velocity.length() < 0.001) {
                return;
            }

            final Rectangled targetBounds;
            if (world.getEntityManager().hasComponent(entity, Collider.class)) {
                val collider = world.getEntityManager().getComponentOf(entity, Collider.class).get();
                targetBounds = updateTargetBoundsWithCollisionDetection(delta, world, entity, transform, collider, tileMapLayers, entitiesWithCollider, velocity.velocity);
            } else {
                velocity.velocity.mul(delta, tmpVelocity);
                targetBounds = transform.bounds.translate(tmpVelocity, new Rectangled());
            }

            applyBounds(targetBounds, transform.bounds);
        });
    }

    private List<TileMap<TileType>> getTileMapLayersWithCollision(@NonNull World world) {
        return world.getEntityManager()
                    .getEntitiesWith(TileMapLayer.class)
                    .map(EntityManager.EntityComponentPair::getComponent)
                    .filter(TileMapLayer::isCollisionEnabled)
                    .map(TileMapLayer::getTileMap)
                    .collect(Collectors.toList());
    }

    private Rectangled updateTargetBoundsWithCollisionDetection(
            final double delta,
            @NonNull final World world,
            @NonNull final Entity entity,
            @NonNull final Transform transform,
            @NonNull final Collider collider,
            @NonNull final List<TileMap<TileType>> tileMapLayers,
            @NonNull final List<Entity> entitiesWithCollider,
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
            val collisions = new ArrayList<CollisionCandidate>();
            val overlaps = new TreeSet<CollisionCandidate>(Comparator.comparingInt(candidate -> candidate.getOther().getId()));
            val distanceMoved = tryMove(world, entity, collider, tileMapLayers, entitiesWithCollider, direction, Math.min(distanceRemaining, stepSize), currentBounds, targetBounds, collisions, overlaps);

            for (val candidate : collisions) {
                if (candidate.isTile()) {
                    fireCollisionEvent(world, entity, collider, Collision.tile(Collision.Mode.COLLISION, candidate.getBounds()));
                } else {
                    fireCollisionEvent(world, entity, collider, Collision.entity(Collision.Mode.COLLISION, candidate.getOther(), candidate.getBounds()));
                    fireCollisionEvent(world, candidate.getOther(), candidate.getOtherCollider(), Collision.entity(Collision.Mode.COLLISION, entity, targetBounds));
                }
            }

            for (val candidate : overlaps) {
                if (!candidate.isTile()) {
                    fireCollisionEvent(world, entity, collider, Collision.entity(Collision.Mode.OVERLAP, candidate.getOther(), candidate.getBounds()));
                    fireCollisionEvent(world, candidate.getOther(), candidate.getOtherCollider(), Collision.entity(Collision.Mode.OVERLAP, entity, targetBounds));
                }
            }

            if (distanceMoved < 0.0) {
                break;
            }
            applyBounds(targetBounds, currentBounds);
            distanceRemaining -= distanceMoved;
        }

        return targetBounds;
    }

    private double tryMove(
            @NonNull final World world,
            @NonNull final Entity entity,
            @NonNull final Collider collider,
            @NonNull final List<TileMap<TileType>> tileMapLayers,
            @NonNull final List<Entity> entitiesWithCollider,
            @NonNull final Vector2d direction,
            final double distance,
            @NonNull final Rectangled initialBounds,
            @NonNull final Rectangled outTargetBounds,
            @NonNull final List<CollisionCandidate> outCollisions,
            @NonNull final Set<CollisionCandidate> outOverlaps
    ) {
        val collisionsXY = new ArrayList<CollisionCandidate>();
        val overlapsXY = new TreeSet<CollisionCandidate>(Comparator.comparingInt(candidate -> candidate.getOther().getId()));
        val distanceMovedXY = checkForCollisionsInDirection(distance, world, entity, collider, direction, tileMapLayers, entitiesWithCollider, initialBounds, outTargetBounds, collisionsXY, overlapsXY);
        if (distanceMovedXY >= 0.0) {
            outOverlaps.addAll(overlapsXY);
            return distanceMovedXY;
        }

        val directionX = new Vector2d(direction.x, 0.0);
        val targetBoundsX = new Rectangled();
        val collisionsX = new ArrayList<CollisionCandidate>();
        val overlapsX = new TreeSet<CollisionCandidate>(Comparator.comparingInt(candidate -> candidate.getOther().getId()));
        val distanceMovedX = checkForCollisionsInDirection(distance, world, entity, collider, directionX, tileMapLayers, entitiesWithCollider, initialBounds, targetBoundsX, collisionsX, overlapsX);

        val directionY = new Vector2d(0.0, direction.y);
        val targetBoundsY = new Rectangled();
        val collisionsY = new ArrayList<CollisionCandidate>();
        val overlapsY = new TreeSet<CollisionCandidate>(Comparator.comparingInt(candidate -> candidate.getOther().getId()));
        val distanceMovedY = checkForCollisionsInDirection(distance, world, entity, collider, directionY, tileMapLayers, entitiesWithCollider, initialBounds, targetBoundsY, collisionsY, overlapsY);

        // Both directions succeeded, move on the axis we can travel furthest
        if (distanceMovedX > 0 && distanceMovedY > 0) {
            if (distanceMovedX > distanceMovedY) {
                applyBounds(targetBoundsX, outTargetBounds);
                outCollisions.addAll(collisionsY);
                outOverlaps.addAll(overlapsY);
                return distanceMovedX;
            } else {
                applyBounds(targetBoundsY, outTargetBounds);
                outCollisions.addAll(collisionsX);
                outOverlaps.addAll(overlapsX);
                return distanceMovedY;
            }
        }
        // Only X succeeded
        else if (distanceMovedX > 0) {
            applyBounds(targetBoundsX,
                        outTargetBounds);
            outCollisions.addAll(collisionsY);
            outOverlaps.addAll(overlapsY);
            return distanceMovedX;
        }
        // Only Y succeeded
        else if (distanceMovedY > 0) {
            applyBounds(targetBoundsY,
                        outTargetBounds);
            outCollisions.addAll(collisionsX);
            outOverlaps.addAll(overlapsX);
            return distanceMovedY;
        }
        // Neither succeeded
        else {
            applyBounds(initialBounds,
                        outTargetBounds);
            outCollisions.addAll(collisionsXY);
            outOverlaps.addAll(overlapsXY);
            return -1.0;
        }
    }

    private double checkForCollisionsInDirection(
            final double distance,
            @NonNull final World world,
            @NonNull final Entity entity,
            @NonNull final Collider collider,
            @NonNull final Vector2d direction,
            @NonNull final List<TileMap<TileType>> tileMapLayers,
            @NonNull final List<Entity> entitiesWithCollider,
            @NonNull final Rectangled initialBounds,
            @NonNull final Rectangled outTargetBounds,
            @NonNull final List<CollisionCandidate> outCollisions,
            @NonNull final Set<CollisionCandidate> outOverlaps
    ) {
        val targetBounds = initialBounds.translate(direction.mul(distance, new Vector2d()), new Rectangled());
        getCollidingTilesAndEntities(world, entity, collider, tileMapLayers, entitiesWithCollider, initialBounds, targetBounds, outCollisions, outOverlaps);

        if (!outCollisions.isEmpty()) {
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

    private void getCollidingTilesAndEntities(
            @NonNull final World world,
            @NonNull final Entity entity,
            @NonNull final Collider collider,
            @NonNull final List<TileMap<TileType>> tileMapLayers,
            @NonNull final List<Entity> entitiesWithCollider,
            @NonNull final Rectangled currentBounds,
            @NonNull final Rectangled targetBounds,
            @NonNull final List<CollisionCandidate> outCollisions,
            @NonNull final Set<CollisionCandidate> outOverlaps
    ) {
        getCollidingTiles(tileMapLayers, currentBounds, targetBounds, outCollisions);
        gatherPossiblyCollidingEntities(world, entitiesWithCollider, entity, targetBounds, collider, outCollisions, outOverlaps);
    }

    private void getCollidingTiles(
            @NonNull final List<TileMap<TileType>> tileMapLayers,
            @NonNull final Rectangled currentBounds,
            @NonNull final Rectangled targetBounds,
            @NonNull final List<CollisionCandidate> outCollisions
    ) {
        val startX = Math.min((int) Math.floor(currentBounds.minX), (int) Math.floor(targetBounds.minX));
        val startY = Math.min((int) Math.floor(currentBounds.minY), (int) Math.floor(targetBounds.minY));
        val endX = Math.max((int) Math.ceil(currentBounds.maxX), (int) Math.ceil(targetBounds.maxX));
        val endY = Math.max((int) Math.ceil(currentBounds.maxY), (int) Math.ceil(targetBounds.maxY));

        val width = endX - startX;
        val height = endY - startY;

        GenerateStream.ofCoordinates(startX, startY, width, height)
                      .filter(pos -> tileMapLayers.stream()
                                                  .map(tm -> tm.getTile(pos))
                                                  .anyMatch(TileType::isSolid))
                      .map(pos -> new Rectangled(pos.x, pos.y, pos.x + 1, pos.y + 1))
                      .filter(targetBounds::intersects)
                      .map(CollisionCandidate::new)
                      .forEach(outCollisions::add);
    }

    private void gatherPossiblyCollidingEntities(
            @NonNull final World world,
            @NonNull final List<Entity> entitiesWithCollider,
            @NonNull final Entity entity,
            @NonNull final Rectangled targetBounds,
            @NonNull final Collider collider,
            @NonNull final List<CollisionCandidate> outCollisions,
            @NonNull final Set<CollisionCandidate> outOverlaps
    ) {
        for (val other : entitiesWithCollider) {
            if (other.getId() == entity.getId()) {
                continue;
            }

            val otherCollider = world.getEntityManager().getComponentOf(other, Collider.class).get();
            val otherTransform = world.getEntityManager().getComponentOf(other, Transform.class).get();

            val intersects = otherTransform.bounds.intersects(targetBounds);
            if (intersects) {
                val candidate = new CollisionCandidate(other, otherTransform.bounds, otherCollider);
                if (otherCollider.isSolidTo(entity, collider)) {
                    outCollisions.add(candidate);
                } else {
                    outOverlaps.add(candidate);
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
        world.getEntityManager().addComponentIfAbsent(entity, new RecentCollisionTag());
    }


    @RequiredArgsConstructor
    private static class CollisionCandidate {
        @Getter private final Entity other;
        @Getter private final Rectangled bounds;
        @Getter private final Collider otherCollider;

        public boolean isTile() {
            return this.other == null || this.otherCollider == null;
        }

        public CollisionCandidate(Rectangled bounds) {
            this.bounds = bounds;
            this.other = null;
            this.otherCollider = null;
        }
    }
}
