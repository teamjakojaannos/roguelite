package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.game.GJK2D;
import fi.jakojaannos.roguelite.game.data.collision.Collision;
import fi.jakojaannos.roguelite.game.data.collision.CollisionEvent;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Math;
import org.joml.Vector2d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Applies velocity read from the {@link Velocity} component to character {@link Transform},
 * handling collisions and firing {@link CollisionEvent Collision Events} whenever necessary.
 * Backbone of the physics and collision detection of characters and other simple moving entities.
 *
 * @see CollisionEvent
 * @see Collision
 */
@Slf4j
public class ApplyVelocitySystem implements ECSSystem {
    /**
     * If velocity length is smaller than this value, applying velocity will be skipped.
     */
    private static final double VELOCITY_EPSILON = 0.000001;

    /**
     * Maximum tries per tick per entity we may attempt to move.
     */
    private static final int MAX_ITERATIONS = 10;

    /**
     * Should an entity move less than this value during an movement iteration, we may consider it
     * being still and can stop trying to move.
     */
    private static final double MOVE_EPSILON = 0.001;

    /**
     * The size of a single movement step. When near collision, this is the resolution at which
     * entities are allowed to move.
     */
    private static final double STEP_SIZE = 0.01;

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
        val entityManager = world.getEntityManager();
        val entitiesWithCollider = entityManager.getEntitiesWith(List.of(Transform.class, Collider.class))
                                                .collect(Collectors.toUnmodifiableList());

        val tileMapLayers = getTileMapLayersWithCollision(world);

        val collisionTargets = new ArrayList<CollisionCandidate>(entitiesWithCollider.size());
        val overlaps = new ArrayList<CollisionCandidate>(entitiesWithCollider.size());
        entities.forEach(entity -> {
            collisionTargets.clear();
            val transform = entityManager.getComponentOf(entity, Transform.class).get();
            val velocity = entityManager.getComponentOf(entity, Velocity.class).get();

            if (velocity.velocity.length() < VELOCITY_EPSILON) {
                return;
            }

            if (entityManager.hasComponent(entity, Collider.class)) {
                val collider = entityManager.getComponentOf(entity, Collider.class).get();


                collectRelevantEntities(entitiesWithCollider,
                                        world,
                                        entity,
                                        collider,
                                        collisionTargets::add);
                val count = collisionTargets.size();
                collectRelevantTiles(tileMapLayers,
                                     transform,
                                     velocity,
                                     collider,
                                     delta,
                                     collisionTargets::add);

                LOG.info("added {} tiles", (collisionTargets.size() - count));

                moveWithCollision(world, entity, transform, velocity, collider, collisionTargets, delta);
            } else {
                moveWithoutCollision(transform, velocity, delta);
            }
        });
    }


    private void collectRelevantTiles(
            final Collection<TileMap<TileType>> tileMapLayers,
            final Transform transform,
            final Velocity velocity,
            final Collider collider,
            final double delta,
            final Consumer<CollisionCandidate> relevantTileConsumer
    ) {
        Shape translatedCollider = (translatedTransform, result) -> {
            collider.getVertices(transform, result);
            collider.getVertices(translatedTransform, result);
            return result;
        };

        val translatedTransform = new Transform(transform.position.x, transform.position.y);
        translatedTransform.position.add(velocity.velocity.mul(delta, new Vector2d()));

        val bounds = translatedCollider.getBounds(new Transform(transform.position.x, transform.position.y));
        val startX = (int) Math.floor(bounds.minX);
        val startY = (int) Math.floor(bounds.minY);
        val endX = (int) Math.ceil(bounds.maxX);
        val endY = (int) Math.ceil(bounds.maxY);

        val width = endX - startX;
        val height = endY - startY;
        for (var ix = 0; ix < width; ++ix) {
            for (var iy = 0; iy < height; ++iy) {
                val x = startX + ix;
                val y = startY + iy;

                tileMapLayers.stream()
                             .map(tm -> tm.getTile(x, y))
                             .filter(TileType::isSolid)
                             .findFirst()
                             .map(tileType -> new CollisionCandidate(x, y, tileType))
                             .ifPresent(relevantTileConsumer);
            }
        }
    }

    private void collectRelevantEntities(
            final List<Entity> entitiesWithCollider,
            final World world,
            final Entity entity,
            final Collider collider,
            final Consumer<CollisionCandidate> relevantEntityConsumer
    ) {
        entitiesWithCollider.stream()
                            .filter(other -> other.getId() != entity.getId())
                            .filter(other -> world.getEntityManager()
                                                  .getComponentOf(other, Collider.class)
                                                  .get()
                                                  .canCollideWith(entity, collider))
                            .map(target -> new CollisionCandidate(target,
                                                                  world.getEntityManager().getComponentOf(target, Transform.class).get(),
                                                                  world.getEntityManager().getComponentOf(target, Collider.class).get()))
                            .forEach(relevantEntityConsumer);
    }

    private void moveWithCollision(
            final World world,
            final Entity entity,
            final Transform transform,
            final Velocity velocity,
            final Collider collider,
            final Collection<CollisionCandidate> collisionTargets,
            final double delta
    ) {
        var distanceRemaining = velocity.velocity.mul(delta, new Vector2d())
                                                 .length();
        val direction = velocity.velocity.normalize(new Vector2d());

        var iterations = 0;
        while (distanceRemaining > 0 && (iterations++) < MAX_ITERATIONS) {
            val distanceMoved = moveUntilCollision(world, entity, transform, collider, direction, distanceRemaining, collisionTargets);

            if (distanceMoved < MOVE_EPSILON) {
                break;
            }
            distanceRemaining -= distanceMoved;
        }
    }

    private double moveUntilCollision(
            final World world,
            final Entity entity,
            final Transform transform,
            final Collider collider,
            final Vector2d direction,
            final double distance,
            final Collection<CollisionCandidate> collisionTargets
    ) {
        List<Vector2d> vertices = new ArrayList<>(8);
        collider.getVertices(transform, vertices);
        vertices.add(null);
        vertices.add(null);
        vertices.add(null);
        vertices.add(null);
        Shape translatedCollider = (translatedTransform, ignored) -> {
            vertices.remove(7);
            vertices.remove(6);
            vertices.remove(5);
            vertices.remove(4);
            collider.getVertices(translatedTransform, vertices);
            return vertices;
        };

        val translatedTransform = new Transform();

        // Fail fast if we cannot move at all
        Optional<CollisionCandidate> collision;
        if ((collision = collisionAfterMoving(entity, collider, STEP_SIZE, direction, transform, translatedTransform, translatedCollider, collisionTargets)).isPresent()) {
            collision.ifPresent(candidate -> fireCollisionEvent(world, entity, collider, candidate));
            return 0.0;
        }

        // Return immediately if we can move the full distance
        if ((collision = collisionAfterMoving(entity, collider, distance, direction, transform, translatedTransform, translatedCollider, collisionTargets)).isEmpty()) {
            collisionTargets.stream()
                            .filter(target -> target.entity != null && ((Collider) target.shape).canOverlapsWith(entity, collider))
                            .filter(target -> target.overlaps(translatedTransform, translatedCollider))
                            .forEach(candidate -> {
                                collider.collisions.add(new CollisionEvent(Collision.entity(Collision.Mode.OVERLAP,
                                                                                            candidate.entity)));
                                ((Collider) candidate.shape).collisions.add(new CollisionEvent(Collision.entity(Collision.Mode.OVERLAP,
                                                                                                                entity)));

                                world.getEntityManager().addComponentIfAbsent(entity, new RecentCollisionTag());
                                world.getEntityManager().addComponentIfAbsent(candidate.entity, new RecentCollisionTag());
                            });

            val translation = direction.mul(distance, new Vector2d());
            transform.position.add(translation);
            return distance;
        }

        // Binary search for maximum steps we are allowed to take
        val maxSteps = (int) (distance / STEP_SIZE);
        int stepsToTake = -1;
        for (int b = maxSteps; b >= 1; b /= 2) {
            while ((collision = collisionAfterMoving(entity, collider, (stepsToTake + b) * STEP_SIZE,
                                                     direction,
                                                     transform,
                                                     translatedTransform,
                                                     translatedCollider,
                                                     collisionTargets)).isEmpty()
            ) {
                stepsToTake += b;
            }
        }

        if (stepsToTake == -1) {
            return 0.0;
        }

        val distanceToMove = stepsToTake * STEP_SIZE;

        // Fire collision/overlap events
        collision.ifPresent(candidate -> fireCollisionEvent(world, entity, collider, candidate));
        translatedTransform.position.set(transform.position)
                                    .add(direction.mul(distanceToMove, new Vector2d()));
        collisionTargets.stream()
                        .filter(target -> target.entity != null && ((Collider) target.shape).canOverlapsWith(entity, collider))
                        .filter(target -> target.overlaps(translatedTransform, translatedCollider))
                        .forEach(candidate -> {
                            collider.collisions.add(new CollisionEvent(Collision.entity(Collision.Mode.OVERLAP,
                                                                                        candidate.entity)));
                            ((Collider) candidate.shape).collisions.add(new CollisionEvent(Collision.entity(Collision.Mode.OVERLAP,
                                                                                                            entity)));

                            world.getEntityManager().addComponentIfAbsent(entity, new RecentCollisionTag());
                            world.getEntityManager().addComponentIfAbsent(candidate.entity, new RecentCollisionTag());
                        });

        val translation = direction.mul(distanceToMove, new Vector2d());
        transform.position.add(translation);

        return distanceToMove;
    }

    private Optional<CollisionCandidate> collisionAfterMoving(
            final Entity entity,
            final Collider collider,
            final double distance,
            final Vector2d direction,
            final Transform transform,
            final Transform translatedTransform,
            final Shape translatedCollider,
            final Collection<CollisionCandidate> collisionTargets
    ) {
        translatedTransform.position.set(transform.position)
                                    .add(direction.mul(distance, new Vector2d()));

        return collisionTargets.stream()
                               .filter(target -> target.isSolidTo(entity, collider))
                               .filter(target -> target.overlaps(translatedTransform, translatedCollider))
                               .findFirst();
    }

    private void fireCollisionEvent(
            final World world,
            final Entity entity,
            final Collider collider,
            final CollisionCandidate candidate
    ) {
        if (candidate.entity == null) {
            collider.collisions.add(new CollisionEvent(Collision.tile(Collision.Mode.COLLISION,
                                                                      candidate.transform.position.x,
                                                                      candidate.transform.position.y)));
        } else {
            collider.collisions.add(new CollisionEvent(Collision.entity(Collision.Mode.COLLISION,
                                                                        candidate.entity)));
            ((Collider) candidate.shape).collisions.add(new CollisionEvent(Collision.entity(Collision.Mode.COLLISION,
                                                                                            entity)));

            world.getEntityManager().addComponentIfAbsent(entity, new RecentCollisionTag());
            world.getEntityManager().addComponentIfAbsent(candidate.entity, new RecentCollisionTag());
        }
    }

    private void moveWithoutCollision(
            final Transform transform,
            final Velocity velocity,
            final double delta
    ) {
        transform.position.add(velocity.velocity.mul(delta, tmpVelocity));
    }

    private List<TileMap<TileType>> getTileMapLayersWithCollision(World world) {
        return world.getEntityManager()
                    .getEntitiesWith(TileMapLayer.class)
                    .map(EntityManager.EntityComponentPair::getComponent)
                    .filter(TileMapLayer::isCollisionEnabled)
                    .map(TileMapLayer::getTileMap)
                    .collect(Collectors.toList());
    }

/*
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
                    fireCollisionEvent(world, entity, collider, Collision.entity(Collision.Mode.COLLISION, candidate.entity));
                    fireCollisionEvent(world, candidate.entity, candidate.collider, Collision.entity(Collision.Mode.COLLISION, entity));
                }
            }

            for (val candidate : overlaps) {
                if (!candidate.isTile()) {
                    fireCollisionEvent(world, entity, collider, Collision.entity(Collision.Mode.OVERLAP, candidate.entity));
                    fireCollisionEvent(world, candidate.entity, candidate.collider, Collision.entity(Collision.Mode.OVERLAP, entity));
                }
            }

            if (distanceMoved <= 0.00001) {
                break;
            }
            currentPosition.set(newTargetPosition);

            // TODO: This is incorrect moved distance. Project last movement onto the initial
            //  translation vector and calculate the length of that projection to get the correct
            //  value.
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
        // FIXME: Do not multiply by delta here, it is already taken in account. Fix the incorrect
        //  relative distance calculation and remove multiplication then.
        val targetPositionX = moveUntilCollisionsInDirection(stepSize, Math.abs(distance * direction.x) * delta, world, entity, transform, collider, directionX, tileMapLayers, entitiesWithCollider, initialPosition, collisionsX, overlapsX);
        val distanceMovedX = initialPosition.distance(targetPositionX);

        val directionY = new Vector2d(0.0, direction.y);
        val collisionsY = new ArrayList<CollisionCandidate>();
        val overlapsY = new HashSet<CollisionCandidate>();
        val targetPositionY = moveUntilCollisionsInDirection(stepSize, Math.abs(distance * direction.y) * delta, world, entity, transform, collider, directionY, tileMapLayers, entitiesWithCollider, initialPosition, collisionsY, overlapsY);
        val distanceMovedY = initialPosition.distance(targetPositionY);

        // Both directions succeeded, move on the axis we can travel furthest
        if (distanceMovedX > 0 && distanceMovedY > 0) {
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
        else if (distanceMovedX > 0) {
            outCollisions.addAll(collisionsY);
            outOverlaps.addAll(overlapsY);
            return targetPositionX;
        }
        // Only Y succeeded
        else if (distanceMovedY > 0) {
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

        val targetPosition = moveUntilCollision(stepSize, distance, direction, world, entity, transform, collider, entitiesWithCollider, tileMapLayers, initialPosition, outCollisions, outOverlaps);
        val distanceUntilCollision = initialPosition.distance(targetPosition);

        val epsilon = 0.00001;
        if (distanceUntilCollision <= epsilon) {
            return new Vector2d(initialPosition);
        } else {
            return new Vector2d(targetPosition);
        }
    }

    private Vector2d moveUntilCollision(
            final double stepSize,
            final double distance,
            final Vector2d direction,
            final World world,
            final Entity entity,
            final Transform transform,
            final Collider collider,
            final List<Entity> entitiesWithCollider,
            final List<TileMap<TileType>> tileMapLayers,
            final Vector2d initialPosition,
            final List<CollisionCandidate> outCollisions,
            final Set<CollisionCandidate> outOverlaps
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
            collision = getTileCollision(collider, tileMapLayers, (int) startX, (int) startY, width, height, nextTransform);

            if (collision.isPresent()) {
                break;
            }

            collision = getEntityCollisionAndOverlaps(world, entitiesWithCollider, entity, collider, outOverlaps, nextTransform);

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

    @Nonnull
    private Optional<CollisionCandidate> getTileCollision(
            Collider collider,
            List<TileMap<TileType>> tileMapLayers,
            int startX,
            int startY,
            long width,
            long height,
            Transform nextTransform
    ) {
        for (var ix = 0; ix < width; ++ix) {
            for (var iy = 0; iy < height; ++iy) {
                val x = startX + ix;
                val y = startY + iy;

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

                if (GJK2D.intersects(nextTransform, collider, tileShape, new Vector2d(x + 0.5, y + 0.5).sub(nextTransform.position).negate())) {
                    return Optional.of(new CollisionCandidate(x, y, null, null));
                }
            }
        }

        return Optional.empty();
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

    private Optional<CollisionCandidate> getEntityCollisionAndOverlaps(
            World world,
            List<Entity> entitiesWithCollider,
            Entity entity,
            Collider collider,
            Set<CollisionCandidate> outOverlaps,
            Transform nextTransform
    ) {
        Optional<CollisionCandidate> collision = Optional.empty();

        for (val other : entitiesWithCollider) {
            if (other.getId() == entity.getId()) {
                continue;
            }

            val otherCollider = world.getEntityManager().getComponentOf(other, Collider.class).get();
            val otherTransform = world.getEntityManager().getComponentOf(other, Transform.class).get();

            if (GJK2D.intersects(nextTransform, collider, otherTransform, otherCollider, new Vector2d(otherTransform.position).sub(nextTransform.position))) {
                if (collision.isEmpty() && otherCollider.isSolidTo(entity, collider)) {
                    collision = Optional.of(new CollisionCandidate(otherTransform.position.x, otherTransform.position.y, other, otherCollider));
                } else {
                    outOverlaps.add(new CollisionCandidate(otherTransform.position.x, otherTransform.position.y, other, otherCollider));
                }
            }
        }
        return collision;
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
*/

    public static final class CollisionCandidate {
        private final Transform transform;
        private final Shape shape;

        @Nullable private final Entity entity;
        @Nullable private final TileType tileType;

        public boolean overlaps(final Transform transform, final Shape shape) {
            val initialDirection = transform.position.sub(this.transform.position, new Vector2d());
            return GJK2D.intersects(transform, shape, this.transform, this.shape, initialDirection);
        }

        public CollisionCandidate(final double x, final double y, final TileType tileType) {
            this.transform = new Transform(x, y);
            this.tileType = tileType;

            this.entity = null;
            this.shape = (ignored, result) -> {
                result.add(new Vector2d(this.transform.position.x + 0, this.transform.position.y + 0));
                result.add(new Vector2d(this.transform.position.x + 1, this.transform.position.y + 0));
                result.add(new Vector2d(this.transform.position.x + 0, this.transform.position.y + 1));
                result.add(new Vector2d(this.transform.position.x + 1, this.transform.position.y + 1));
                return result;
            };
        }

        public CollisionCandidate(
                final Entity entity,
                final Transform transform,
                final Collider collider
        ) {
            this.entity = entity;
            this.transform = transform;
            this.shape = collider;

            this.tileType = null;
        }

        public int otherId() {
            return this.entity == null
                    ? -1
                    : this.entity.getId();
        }

        public boolean isSolidTo(Entity entity, Collider collider) {
            return (this.tileType != null && this.tileType.isSolid())
                    || (this.entity != null && ((Collider) this.shape).isSolidTo(entity, collider));
        }
    }
}
