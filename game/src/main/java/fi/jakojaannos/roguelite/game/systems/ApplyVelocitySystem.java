package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.game.GJK2D;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;
import fi.jakojaannos.roguelite.game.systems.collision.Collision;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Math;
import org.joml.Vector2d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
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
                    .withComponent(Velocity.class)
                    .requireResource(Colliders.class);
    }

    private final Vector2d tmpVelocity = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world,
            final double delta
    ) {
        val entityManager = world.getEntityManager();
        val entitiesWithCollider = world.getResource(Colliders.class);

        val tileMapLayers = getTileMapLayersWithCollision(world);

        val collisionTargets = new ArrayList<CollisionCandidate>();
        val overlapTargets = new ArrayList<CollisionCandidate>();
        entities.forEach(entity -> {
            collisionTargets.clear();
            overlapTargets.clear();
            val transform = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
            val velocity = entityManager.getComponentOf(entity, Velocity.class).orElseThrow();

            if (velocity.velocity.length() < VELOCITY_EPSILON) {
                return;
            }

            if (entityManager.hasComponent(entity, Collider.class)) {
                val collider = entityManager.getComponentOf(entity, Collider.class).orElseThrow();


                val translatedTransform = new Transform(transform);
                translatedTransform.position.add(velocity.velocity.mul(delta, new Vector2d()));
                val vertices = new Vector2d[8];
                System.arraycopy(collider.getVerticesInLocalSpace(transform), 0, vertices, 0, 4);
                System.arraycopy(collider.getVerticesInLocalSpace(translatedTransform), 0, vertices, 4, 4);
                Shape translatedCollider = (ignored) -> vertices;


                collectRelevantTiles(tileMapLayers,
                                     transform,
                                     velocity,
                                     collider,
                                     translatedTransform,
                                     translatedCollider,
                                     delta,
                                     collisionTargets::add,
                                     overlapTargets::add);
                collectRelevantEntities(entitiesWithCollider,
                                        world,
                                        entity,
                                        collider,
                                        translatedTransform,
                                        translatedCollider,
                                        collisionTargets::add,
                                        overlapTargets::add);

                moveWithCollision(world, entity, transform, velocity, collider, collisionTargets, overlapTargets, delta);
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
            final Transform translatedTransform,
            final Shape translatedCollider,
            final double delta,
            final Consumer<CollisionCandidate> colliderConsumer,
            final Consumer<CollisionCandidate> overlapConsumer
    ) {
        val bounds = translatedCollider.getBounds(translatedTransform);
        val startX = (int) Math.floor(bounds.minX - STEP_SIZE);
        val startY = (int) Math.floor(bounds.minY - STEP_SIZE);
        val endX = (int) Math.ceil(bounds.maxX + STEP_SIZE);
        val endY = (int) Math.ceil(bounds.maxY + STEP_SIZE);

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
                             .ifPresent(colliderConsumer);
            }
        }
    }

    private void collectRelevantEntities(
            final Colliders entitiesWithCollider,
            final World world,
            final Entity entity,
            final Collider collider,
            final Transform translatedTransform,
            final Shape translatedCollider,
            final Consumer<CollisionCandidate> colliderConsumer,
            final Consumer<CollisionCandidate> overlapConsumer
    ) {
        entitiesWithCollider.solidForLayer.computeIfAbsent(collider.layer, key -> List.of())
                                          .stream()
                                          .filter(other -> other.getId() != entity.getId())
                                          .map(other -> new CollisionCandidate(other,
                                                                               world.getEntityManager().getComponentOf(other, Transform.class).orElseThrow(),
                                                                               world.getEntityManager().getComponentOf(other, Collider.class).orElseThrow()))
                                          .forEach(colliderConsumer);
        entitiesWithCollider.overlapsWithLayer.computeIfAbsent(collider.layer, key -> List.of())
                                              .stream()
                                              .filter(other -> other.getId() != entity.getId())
                                              .map(other -> new CollisionCandidate(other,
                                                                                   world.getEntityManager().getComponentOf(other, Transform.class).orElseThrow(),
                                                                                   world.getEntityManager().getComponentOf(other, Collider.class).orElseThrow()))
                                              .forEach(overlapConsumer);

    }

    private void moveWithCollision(
            final World world,
            final Entity entity,
            final Transform transform,
            final Velocity velocity,
            final Collider collider,
            final Collection<CollisionCandidate> collisionTargets,
            final Collection<CollisionCandidate> overlapTargets,
            final double delta
    ) {
        var distanceRemaining = velocity.velocity.mul(delta, new Vector2d())
                                                 .length();
        val direction = velocity.velocity.normalize(new Vector2d());

        var iterations = 0;
        val collisions = new ArrayList<CollisionEventCandidate>();
        val collisionsX = new ArrayList<CollisionEventCandidate>();
        val collisionsY = new ArrayList<CollisionEventCandidate>();
        val tmpTransform = new Transform(transform);
        val tmpTransformX = new Transform(transform);
        val tmpTransformY = new Transform(transform);
        while (distanceRemaining > 0 && (iterations++) < MAX_ITERATIONS) {
            collisions.clear();
            collisionsX.clear();
            collisionsY.clear();
            tmpTransform.set(transform);
            tmpTransformX.set(transform);
            tmpTransformY.set(transform);

            var distanceMoved = moveUntilCollision(entity,
                                                   tmpTransform,
                                                   collider,
                                                   direction,
                                                   distanceRemaining,
                                                   collisionTargets,
                                                   overlapTargets,
                                                   (candidate, mode) -> collisions.add(new CollisionEventCandidate(mode, candidate)));

            var actualCollisions = collisions;
            if (distanceMoved < MOVE_EPSILON) {
                double distanceMovedX = 0.0;
                if (Math.abs(direction.x) > VELOCITY_EPSILON) {
                    distanceMovedX = moveUntilCollision(entity,
                                                        tmpTransformX,
                                                        collider,
                                                        new Vector2d(direction.x, 0.0).normalize(),
                                                        Math.abs(distanceRemaining * direction.x),
                                                        collisionTargets,
                                                        overlapTargets,
                                                        (candidate, mode) -> collisionsX.add(new CollisionEventCandidate(mode, candidate)));
                }

                double distanceMovedY = 0.0;
                if (Math.abs(direction.y) > VELOCITY_EPSILON) {
                    distanceMovedY = moveUntilCollision(entity,
                                                        tmpTransformY,
                                                        collider,
                                                        new Vector2d(0.0, direction.y).normalize(),
                                                        Math.abs(distanceRemaining * direction.y),
                                                        collisionTargets,
                                                        overlapTargets,
                                                        (candidate, mode) -> collisionsY.add(new CollisionEventCandidate(mode, candidate)));
                }

                if (distanceMovedX > MOVE_EPSILON) {
                    distanceMoved = distanceMovedX;
                    tmpTransform.set(tmpTransformX);
                    actualCollisions = collisionsY;
                } else if (distanceMovedY > MOVE_EPSILON) {
                    distanceMoved = distanceMovedY;
                    tmpTransform.set(tmpTransformY);
                    actualCollisions = collisionsX;
                }
            }

            actualCollisions.forEach(collision -> fireCollisionEvent(world, entity, collider, collision.candidate, collision.mode));
            if (distanceMoved < MOVE_EPSILON) {
                break;
            }

            transform.set(tmpTransform);
            distanceRemaining -= distanceMoved;
        }
    }

    private double moveUntilCollision(
            final Entity entity,
            final Transform transform,
            final Collider collider,
            final Vector2d direction,
            final double distance,
            final Collection<CollisionCandidate> collisionTargets,
            final Collection<CollisionCandidate> overlapTargets,
            final BiConsumer<CollisionCandidate, Collision.Mode> collisionConsumer
    ) {
        Vector2d[] vertices = new Vector2d[8];
        System.arraycopy(collider.getVerticesInLocalSpace(transform), 0, vertices, 0, 4);
        Shape translatedCollider = (translatedTransform) -> {
            val translatedVertices = collider.getVerticesInLocalSpace(translatedTransform);
            vertices[4] = translatedVertices[0];
            vertices[5] = translatedVertices[1];
            vertices[6] = translatedVertices[2];
            vertices[7] = translatedVertices[3];
            return vertices;
        };

        val translatedTransform = new Transform(transform);

        // Fail fast if we cannot move at all
        Optional<CollisionCandidate> collision;
        if ((collision = collisionsAfterMoving(entity, collider, STEP_SIZE, direction, transform, translatedTransform, translatedCollider, collisionTargets)).isPresent()) {
            collision.ifPresent(candidate -> collisionConsumer.accept(candidate, Collision.Mode.COLLISION));
            moveDistanceTriggeringCollisions(transform,
                                             direction,
                                             0.0,
                                             translatedTransform,
                                             translatedCollider,
                                             overlapTargets,
                                             collisionConsumer);
            return 0.0;
        }

        // Return immediately if we can move the full distance
        if ((collision = collisionsAfterMoving(entity, collider, distance, direction, transform, translatedTransform, translatedCollider, collisionTargets)).isEmpty()) {
            moveDistanceTriggeringCollisions(transform,
                                             direction,
                                             distance,
                                             translatedTransform,
                                             translatedCollider,
                                             overlapTargets,
                                             collisionConsumer);
            return distance;
        }

        // Binary search for maximum steps we are allowed to take
        val maxSteps = (int) (distance / STEP_SIZE);
        int stepsToTake = -1;
        for (int b = maxSteps; b >= 1; b /= 2) {
            while (stepsToTake <= maxSteps // borderline case of full distance leading to collision and maxSteps not colliding
                    && (collision = collisionsAfterMoving(entity,
                                                          collider,
                                                          (stepsToTake + b) * STEP_SIZE,
                                                          direction,
                                                          transform,
                                                          translatedTransform,
                                                          translatedCollider,
                                                          collisionTargets)).isEmpty()
            ) {
                stepsToTake += b;
            }
        }

        // Just in case, needed for borderline cases
        if (stepsToTake > maxSteps) {
            stepsToTake = maxSteps;
        }

        if (stepsToTake == -1) {
            LOG.warn("Could not move. This should have been covered by early checks!");
            return 0.0;
        }

        val distanceToMove = stepsToTake * STEP_SIZE;
        collision.ifPresent(candidate -> collisionConsumer.accept(candidate, Collision.Mode.COLLISION));
        moveDistanceTriggeringCollisions(transform,
                                         direction,
                                         distanceToMove,
                                         translatedTransform,
                                         translatedCollider,
                                         overlapTargets,
                                         collisionConsumer);

        return distanceToMove;
    }

    private void moveDistanceTriggeringCollisions(
            final Transform transform,
            final Vector2d direction,
            final double distanceToMove,
            final Transform translatedTransform,
            final Shape translatedCollider,
            final Collection<CollisionCandidate> overlapTargets,
            final BiConsumer<CollisionCandidate, Collision.Mode> collisionConsumer
    ) {
        overlapTargets.stream()
                      .filter(target -> target.overlaps(translatedTransform, translatedCollider))
                      .forEach(candidate -> collisionConsumer.accept(candidate, Collision.Mode.OVERLAP));

        val translation = direction.mul(distanceToMove, new Vector2d());
        transform.position.add(translation);
    }

    private Optional<CollisionCandidate> collisionsAfterMoving(
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
                               //.filter(target -> target.isSolidTo(entity, collider))
                               .filter(target -> target.overlaps(translatedTransform, translatedCollider))
                               .findFirst();
    }

    private void fireCollisionEvent(
            final World world,
            final Entity entity,
            final Collider collider,
            final CollisionCandidate candidate,
            final Collision.Mode mode
    ) {
        if (candidate.entity == null) {
            collider.collisions.add(new CollisionEvent(Collision.tile(mode,
                                                                      candidate.transform.position.x,
                                                                      candidate.transform.position.y)));
        } else {
            collider.collisions.add(new CollisionEvent(Collision.entity(mode,
                                                                        candidate.entity)));
            ((Collider) candidate.shape).collisions.add(new CollisionEvent(Collision.entity(mode,
                                                                                            entity)));

            world.getEntityManager().addComponentIfAbsent(candidate.entity, new RecentCollisionTag());
        }

        world.getEntityManager().addComponentIfAbsent(entity, new RecentCollisionTag());
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

    public static final class CollisionCandidate {
        private static final Vector2d[] TILE_VERTICES = new Vector2d[]{
                new Vector2d(0, 0),
                new Vector2d(1, 0),
                new Vector2d(0, 1),
                new Vector2d(1, 1)
        };

        private final Transform transform;
        private final Shape shape;

        @Nullable private final Entity entity;
        @Nullable private final TileType tileType;

        public boolean overlaps(final Transform transform, final Shape shape) {
            val initialDirection = transform.position.sub(this.transform.position, new Vector2d());
            return GJK2D.intersects(transform, shape, this.transform, this.shape, initialDirection);
        }

        public CollisionCandidate(
                final double x,
                final double y,
                final TileType tileType
        ) {
            this.transform = new Transform(x, y);
            this.tileType = tileType;

            this.entity = null;
            this.shape = ignored -> TILE_VERTICES;
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

        public boolean isSolidTo(Collider collider) {
            return (this.tileType != null && this.tileType.isSolid())
                    || (this.entity != null && ((Collider) this.shape).isSolidTo(collider));
        }
    }

    @RequiredArgsConstructor
    private static class CollisionEventCandidate {
        private final Collision.Mode mode;
        private final CollisionCandidate candidate;
    }
}
