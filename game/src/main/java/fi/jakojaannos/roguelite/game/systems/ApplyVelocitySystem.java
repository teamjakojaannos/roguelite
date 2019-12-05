package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.engine.utilities.math.RotatedRectangle;
import fi.jakojaannos.roguelite.game.GJK2D;
import fi.jakojaannos.roguelite.game.data.collision.Collision;
import fi.jakojaannos.roguelite.game.data.collision.CollisionEvent;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Math;
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
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.PHYSICS_TICK)
                    .withComponent(Transform.class)
                    .withComponent(Velocity.class);
    }

    private final Vector2d tmpVelocity = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world,
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

            val targetPosition = world.getEntityManager()
                                      .getComponentOf(entity, Collider.class)
                                      .map(collider -> updateTargetBoundsWithCollisionDetection(delta,
                                                                                                world,
                                                                                                entity,
                                                                                                transform,
                                                                                                collider,
                                                                                                tileMapLayers,
                                                                                                entitiesWithCollider,
                                                                                                velocity.velocity))
                                      .orElseGet(() -> transform.position.add(velocity.velocity.mul(delta,
                                                                                                    tmpVelocity),
                                                                              new Vector2d()));

            transform.position.set(targetPosition);
        });
    }

    private List<TileMap<TileType>> getTileMapLayersWithCollision(World world) {
        return world.getEntityManager()
                    .getEntitiesWith(TileMapLayer.class)
                    .map(EntityManager.EntityComponentPair::getComponent)
                    .filter(TileMapLayer::isCollisionEnabled)
                    .map(TileMapLayer::getTileMap)
                    .collect(Collectors.toList());
    }

    private Vector2d updateTargetBoundsWithCollisionDetection(
            final double delta,
            final World world,
            final Entity entity,
            final Transform transform,
            final Collider collider,
            final List<TileMap<TileType>> tileMapLayers,
            final List<Entity> entitiesWithCollider,
            final Vector2d velocityRaw
    ) {
        val direction = velocityRaw.normalize(new Vector2d());
        val maxDistance = velocityRaw.length() * delta;

        val initialPosition = transform.position;
        val stepSize = 1.0;
        val smallStepSize = 0.01;

        var distanceRemaining = maxDistance;
        val currentPosition = new Vector2d(initialPosition);
        while (distanceRemaining > 0.0) {
            val collisions = new ArrayList<CollisionCandidate>();
            val overlaps = new HashSet<CollisionCandidate>();
            val newTargetPosition = tryMove(delta, smallStepSize, world, entity, transform, collider, tileMapLayers, entitiesWithCollider, direction, Math.min(distanceRemaining, stepSize), currentPosition, collisions, overlaps);
            val distanceMoved = currentPosition.distance(newTargetPosition);

            for (val candidate : collisions) {
                if (candidate.isTile()) {
                    //fireCollisionEvent(world, entity, collider, Collision.tile(Collision.Mode.COLLISION));
                } else {
                    fireCollisionEvent(world, entity, collider, Collision.entity(Collision.Mode.COLLISION, candidate.other));
                    fireCollisionEvent(world, candidate.other, candidate.otherCollider, Collision.entity(Collision.Mode.COLLISION, entity));
                }
            }

            for (val candidate : overlaps) {
                if (!candidate.isTile()) {
                    fireCollisionEvent(world, entity, collider, Collision.entity(Collision.Mode.OVERLAP, candidate.other));
                    fireCollisionEvent(world, candidate.other, candidate.otherCollider, Collision.entity(Collision.Mode.OVERLAP, entity));
                }
            }

            if (distanceMoved <= smallStepSize / 10.0) {
                break;
            }
            currentPosition.set(newTargetPosition);
            distanceRemaining -= distanceMoved;
        }

        return currentPosition;
    }

    private Vector2d tryMove(
            final double delta,
            final double stepSize,
            final World world,
            final Entity entity,
            final Transform transform,
            final Collider collider,
            final List<TileMap<TileType>> tileMapLayers,
            final List<Entity> entitiesWithCollider,
            final Vector2d direction,
            final double distance,
            final Vector2d initialPosition,
            final List<CollisionCandidate> outCollisions,
            final Set<CollisionCandidate> outOverlaps
    ) {
        val collisionsXY = new ArrayList<CollisionCandidate>();
        val overlapsXY = new HashSet<CollisionCandidate>();
        val targetPositionXY = moveUntilCollisionsInDirection(stepSize, distance, world, entity, transform, collider, direction, tileMapLayers, entitiesWithCollider, initialPosition, collisionsXY, overlapsXY);
        val distanceMovedXY = initialPosition.distance(targetPositionXY);
        if (distanceMovedXY > 0) {
            outCollisions.addAll(collisionsXY);
            outOverlaps.addAll(overlapsXY);
            return targetPositionXY;
        }

        val directionX = new Vector2d(direction.x, 0.0);
        val collisionsX = new ArrayList<CollisionCandidate>();
        val overlapsX = new HashSet<CollisionCandidate>();
        val targetPositionX = moveUntilCollisionsInDirection(stepSize, Math.abs(distance * direction.x) * delta, world, entity, transform, collider, directionX, tileMapLayers, entitiesWithCollider, initialPosition, collisionsX, overlapsX);
        val distanceMovedX = initialPosition.distance(targetPositionX);

        val directionY = new Vector2d(0.0, direction.y);
        val collisionsY = new ArrayList<CollisionCandidate>();
        val overlapsY = new HashSet<CollisionCandidate>();
        val targetPositionY = moveUntilCollisionsInDirection(stepSize, Math.abs(distance * direction.y) * delta, world, entity, transform, collider, directionY, tileMapLayers, entitiesWithCollider, initialPosition, collisionsY, overlapsY);
        val distanceMovedY = initialPosition.distance(targetPositionY);

        // Both directions succeeded, move on the axis we can travel furthest
        val epsilon = stepSize / 10.0;
        if (distanceMovedX > epsilon && distanceMovedY > epsilon) {
            if (distanceMovedX > distanceMovedY) {
                outCollisions.addAll(collisionsY);
                outOverlaps.addAll(overlapsY);
                return targetPositionX;
            } else {
                outCollisions.addAll(collisionsX);
                outOverlaps.addAll(overlapsX);
                return targetPositionY;
            }
        }
        // Only X succeeded
        else if (distanceMovedX > epsilon) {
            outCollisions.addAll(collisionsY);
            outOverlaps.addAll(overlapsY);
            return targetPositionX;
        }
        // Only Y succeeded
        else if (distanceMovedY > epsilon) {
            outCollisions.addAll(collisionsX);
            outOverlaps.addAll(overlapsX);
            return targetPositionY;
        }
        // Neither succeeded
        else {
            outCollisions.addAll(collisionsXY);
            outOverlaps.addAll(overlapsXY);
            return new Vector2d(initialPosition);
        }
    }

    private Vector2d moveUntilCollisionsInDirection(
            final double stepSize,
            final double distance,
            final World world,
            final Entity entity,
            final Transform transform,
            final Collider collider,
            final Vector2d direction,
            final List<TileMap<TileType>> tileMapLayers,
            final List<Entity> entitiesWithCollider,
            final Vector2d initialPosition,
            final List<CollisionCandidate> outCollisions,
            final Set<CollisionCandidate> outOverlaps
    ) {
        if (direction.lengthSquared() == 0.0) {
            return new Vector2d(initialPosition);
        }

        val tileTargetPosition = moveUntilTileCollision(stepSize, distance, direction, transform, collider, tileMapLayers, initialPosition, outCollisions);
        val distanceUntilTileCollision = initialPosition.distanceSquared(tileTargetPosition);

        val entityTargetPosition = moveUntilEntityCollision(stepSize, distance, direction, world, entitiesWithCollider, entity, collider, initialPosition, outCollisions, outOverlaps);
        val distanceUntilEntityCollision = initialPosition.distanceSquared(entityTargetPosition);

        val epsilon = 0.00001;
        if (distanceUntilTileCollision <= epsilon || distanceUntilEntityCollision <= epsilon) {
            return new Vector2d(initialPosition);
        } else if (distanceUntilTileCollision < distanceUntilEntityCollision) {
            return new Vector2d(tileTargetPosition);
        } else {
            return new Vector2d(entityTargetPosition);
        }
    }

    private Vector2d moveUntilTileCollision(
            final double stepSize,
            final double distance,
            final Vector2d direction,
            final Transform transform,
            final Collider collider,
            final List<TileMap<TileType>> tileMapLayers,
            final Vector2d initialPosition,
            final List<CollisionCandidate> outCollisions
    ) {
        // TODO: Create "stretched" collider
        val targetPosition = initialPosition.add(direction.mul(distance,
                                                               new Vector2d()),
                                                 new Vector2d());

        val currentBounds = new RotatedRectangle(initialPosition, collider.origin, collider.width + stepSize / 2.0, collider.height + stepSize / 2.0, transform.rotation);
        val targetBounds = new RotatedRectangle(targetPosition, collider.origin, collider.width + stepSize / 2.0, collider.height + stepSize / 2.0, transform.rotation);
        currentBounds.position.sub(stepSize / 2.0, stepSize / 2.0);
        targetBounds.position.sub(stepSize / 2.0, stepSize / 2.0);

        val currentMin = new Vector2d();
        val currentMax = new Vector2d();
        findMinMax(currentBounds, currentMin, currentMax);

        val targetMin = new Vector2d();
        val targetMax = new Vector2d();
        findMinMax(targetBounds, targetMin, targetMax);

        val min = currentMin.min(targetMin).floor();
        val max = currentMax.max(targetMax).ceil();

        val startX = org.joml.Math.round(min.x) - 1;
        val startY = org.joml.Math.round(min.y) - 1;
        val endX = org.joml.Math.round(max.x) + 1;
        val endY = org.joml.Math.round(max.y) + 1;

        val width = endX - startX;
        val height = endY - startY;

        val actualStepSize = Math.min(stepSize, distance);
        val step = direction.normalize(actualStepSize, new Vector2d());

        var distanceMoved = 0.0;
        val nextTransform = new Transform(initialPosition.x, initialPosition.y);
        nextTransform.rotation = 0.0;

        Optional<CollisionCandidate> collision = Optional.empty();

        val newPosition = new Vector2d(initialPosition);
        nextTransform.position.add(step); // nextTransform is always a step ahead
        while (distanceMoved <= distance) {
            for (var ix = 0; ix < width; ++ix) {
                for (var iy = 0; iy < height; ++iy) {
                    val x = (int) startX + ix;
                    val y = (int) startY + iy;

                    val notSolid = tileMapLayers.stream()
                                                .map(tm -> tm.getTile(x, y))
                                                .noneMatch(TileType::isSolid);
                    if (notSolid) {
                        continue;
                    }

                    Shape tileShape = (ignored, result) -> {
                        result.add(new Vector2d(x + 0, y + 0));
                        result.add(new Vector2d(x + 1, y + 0));
                        result.add(new Vector2d(x + 0, y + 1));
                        result.add(new Vector2d(x + 1, y + 1));
                        return result;
                    };

                    if (GJK2D.intersects(nextTransform, collider, tileShape, new Vector2d(x + 0.5, y + 0.5).sub(nextTransform.position))) {
                        collision = Optional.of(new CollisionCandidate(x, y, null, null));
                        break;
                    }
                }

                if (collision.isPresent()) {
                    break;
                }
            }

            if (collision.isPresent()) {
                break;
            }

            newPosition.add(step);
            nextTransform.position.add(step);
            distanceMoved += actualStepSize;
        }


        collision.ifPresent(outCollisions::add);
        return newPosition;
    }

    private void findMinMax(
            final RotatedRectangle bounds,
            final Vector2d outMin,
            final Vector2d outMax
    ) {
        Vector2d temp = new Vector2d();
        bounds.getBottomLeft(temp)
              .min(bounds.getBottomRight(temp), outMin)
              .min(bounds.getTopLeft(temp), outMin)
              .min(bounds.getTopRight(temp), outMin);
        bounds.getBottomLeft(temp)
              .max(bounds.getBottomRight(temp), outMax)
              .max(bounds.getTopLeft(temp), outMax)
              .max(bounds.getTopRight(temp), outMax);
    }

    private Vector2d moveUntilEntityCollision(
            final double stepSize,
            final double distance,
            final Vector2d direction,
            final World world,
            final List<Entity> entitiesWithCollider,
            final Entity entity,
            final Collider collider,
            final Vector2d initialPosition,
            final List<CollisionCandidate> outCollisions,
            final Set<CollisionCandidate> outOverlaps
    ) {
        val actualStepSize = Math.min(stepSize, distance);
        val step = direction.normalize(actualStepSize, new Vector2d());

        var distanceMoved = 0.0;
        val newPosition = new Vector2d(initialPosition);
        val nextTransform = new Transform(initialPosition.x, initialPosition.y);
        nextTransform.rotation = 0.0;

        nextTransform.position.add(step);
        Optional<CollisionCandidate> collision = Optional.empty();
        while (distanceMoved <= distance) {
            for (val other : entitiesWithCollider) {
                if (other.getId() == entity.getId()) {
                    continue;
                }

                val otherCollider = world.getEntityManager().getComponentOf(other, Collider.class).get();
                val otherTransform = world.getEntityManager().getComponentOf(other, Transform.class).get();

                if (GJK2D.intersects(nextTransform, collider, otherTransform, otherCollider, new Vector2d(otherTransform.position).sub(nextTransform.position))) {
                    if (otherCollider.isSolidTo(entity, collider)) {
                        collision = Optional.of(new CollisionCandidate(otherTransform.position.x, otherTransform.position.y, other, otherCollider));
                    } else {
                        outOverlaps.add(new CollisionCandidate(otherTransform.position.x, otherTransform.position.y, other, otherCollider));
                    }
                }
            }

            if (collision.isEmpty()) {
                newPosition.add(step);
            }

            nextTransform.position.add(step);
            distanceMoved += actualStepSize;
        }

        collision.ifPresent(outCollisions::add);
        return newPosition;
    }

    private void fireCollisionEvent(
            final World world,
            final Entity entity,
            final Collider collider,
            final Collision collision
    ) {
        collider.collisions.add(new CollisionEvent(collision));
        world.getEntityManager().addComponentIfAbsent(entity, new RecentCollisionTag());
    }


    @RequiredArgsConstructor
    private static class CollisionCandidate {
        private final double x;
        private final double y;
        private final Entity other;
        private final Collider otherCollider;

        public int otherId() {
            return this.other == null
                    ? -1
                    : this.other.getId();
        }

        public boolean isTile() {
            return this.other == null;
        }
    }
}
