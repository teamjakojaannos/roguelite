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
import lombok.val;
import org.joml.Rectangled;
import org.joml.Vector2d;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplyVelocitySystem implements ECSSystem {
    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return List.of(Transform.class, Velocity.class);
    }

    private final Vector2d tmpVelocity = new Vector2d();
    private final Vector2d tmpDirection = new Vector2d();
    private final Rectangled tmpTargetBounds = new Rectangled();
    private final Rectangled tmpTestTargetBounds = new Rectangled();

    @Override
    public void tick(
            @NonNull final Stream<Entity> entities,
            @NonNull final World world,
            final double delta
    ) {
        val entitiesWithCollider = world.getEntities()
                                        .getEntitiesWith(List.of(Collider.class, Transform.class))
                                        .collect(Collectors.toUnmodifiableList());

        val tileMapLayers = world.getEntities()
                                 .getEntitiesWith(TileMapLayer.class)
                                 .map(Entities.EntityComponentPair::getComponent)
                                 .filter(TileMapLayer::isCollisionEnabled)
                                 .map(TileMapLayer::getTileMap)
                                 .collect(Collectors.toList());

        entities.forEach(entity -> {
            val transform = world.getEntities().getComponentOf(entity, Transform.class).get();
            val velocity = world.getEntities().getComponentOf(entity, Velocity.class).get();


            var targetBounds = calculateDestinationBounds(transform, velocity, delta);
            if (world.getEntities().hasComponent(entity, Collider.class)) {
                val collider = world.getEntities().getComponentOf(entity, Collider.class).get();
                targetBounds = moveWithCollisionHandling(world, entitiesWithCollider, tileMapLayers, entity, transform, velocity, collider, targetBounds);
            }

            move(transform, targetBounds);
        });
    }

    private void move(
            @NonNull final Transform transform,
            @NonNull final Rectangled targetBounds
    ) {
        transform.bounds.minX = targetBounds.minX;
        transform.bounds.minY = targetBounds.minY;
        transform.bounds.maxX = targetBounds.maxX;
        transform.bounds.maxY = targetBounds.maxY;
    }

    private Rectangled moveWithCollisionHandling(
            @NonNull final World world,
            @NonNull final List<Entity> entitiesWithCollider,
            @NonNull final List<TileMap<TileType>> tileMapLayers,
            @NonNull final Entity entity,
            @NonNull final Transform transform,
            @NonNull final Velocity velocity,
            @NonNull final Collider collider,
            @NonNull Rectangled targetBounds
    ) {
        val collisions = new ArrayList<CollisionCandidate>();
        val overlaps = new ArrayList<CollisionCandidate>();

        // TODO: Now, we are checking only coordinates we would end up after moving all
        //  the way to the targetBounds. Stretch the collider and do more refined checks
        //  to catch more collision cases with fast-moving entities.
        gatherPossiblyCollidingEntities(world, entitiesWithCollider, entity, transform, collider, collisions, overlaps);
        gatherOverlappingTileBounds(tileMapLayers, transform.bounds, targetBounds, collisions);

        if (!collisions.isEmpty()) {
            targetBounds = moveUntilCollision(world,
                                              entity,
                                              collider,
                                              transform.bounds,
                                              velocity.velocity,
                                              collisions,
                                              overlaps,
                                              targetBounds);
        }

        return targetBounds;
    }

    private Rectangled calculateDestinationBounds(
            @NonNull final Transform transform,
            @NonNull final Velocity velocity,
            final double delta
    ) {
        velocity.velocity.mul(delta, tmpVelocity);
        return transform.bounds.translate(tmpVelocity, tmpTargetBounds);
    }

    private Rectangled moveUntilCollision(
            @NonNull final World world,
            @NonNull final Entity entity,
            @NonNull final Collider collider,
            @NonNull final Rectangled initialBounds,
            @NonNull final Vector2d velocity,
            @NonNull final List<CollisionCandidate> collisions,
            @NonNull final List<CollisionCandidate> overlaps,
            @NonNull Rectangled targetBounds
    ) {
        val actualOverlaps = new TreeSet<CollisionCandidate>(Comparator.comparing(candidate -> candidate.getOther().getId()));
        val stepSize = 0.01;
        val maxDistance = velocity.length();
        val direction = velocity.normalize(tmpDirection);
        var distanceRemaining = maxDistance;
        var currentBounds = new Rectangled(initialBounds);
        while (distanceRemaining > stepSize) {
            val distanceMoved = tryMove(world, entity, collider, stepSize, direction, distanceRemaining, currentBounds, collisions, overlaps, actualOverlaps, targetBounds);

            if (distanceMoved < stepSize) {
                break;
            }
            currentBounds = new Rectangled(targetBounds);
            distanceRemaining -= distanceMoved;
        }

        for (val candidate : actualOverlaps) {
            assert !candidate.isTile();
            fireCollisionEvent(world, entity, collider, Collision.entity(candidate.other, candidate.getOtherBounds()));
            fireCollisionEvent(world, candidate.other, candidate.getOtherCollider(), Collision.entity(entity, targetBounds));
        }

        return targetBounds;
    }

    private double tryMove(
            World world,
            Entity entity,
            Collider collider,
            double stepSize,
            Vector2d direction,
            double distance,
            Rectangled initialBounds,
            List<CollisionCandidate> collisions,
            List<CollisionCandidate> overlaps,
            Set<CollisionCandidate> actualOverlaps,
            Rectangled targetBounds
    ) {
        var steps = 0;
        while (steps * stepSize < distance) {
            val testVelocity = direction.mul((steps + 1) * stepSize, tmpVelocity);
            val testTargetBounds = initialBounds.translate(testVelocity, tmpTestTargetBounds);

            val actualCollision = collisions.stream()
                                            .filter(collision -> collision.getOtherBounds().intersects(testTargetBounds))
                                            .findFirst();
            if (actualCollision.isEmpty()) {
                gatherOverlapEvents(overlaps, testTargetBounds, actualOverlaps);
            } else {
                val actualVelocity = direction.mul(steps * stepSize, tmpVelocity);
                initialBounds.translate(actualVelocity, targetBounds);

                handleCollision(world, entity, collider, targetBounds, actualCollision.get(), overlaps);
                break;
            }
            ++steps;
        }
        return steps * stepSize;
    }

    private void gatherOverlapEvents(
            @NonNull final List<CollisionCandidate> overlaps,
            @NonNull final Rectangled testTargetBounds,
            @NonNull Set<CollisionCandidate> actualOverlaps
    ) {
        overlaps.stream()
                .filter(overlap -> overlap.getOtherBounds().intersects(testTargetBounds))
                .forEach(actualOverlaps::add);
    }

    private void handleCollision(
            @NonNull final World world,
            @NonNull final Entity entity,
            @NonNull final Collider collider,
            @NonNull final Rectangled newTargetBounds,
            @NonNull final CollisionCandidate collision,
            @NonNull final List<CollisionCandidate> overlaps
    ) {
        if (collision.isTile()) {
            fireCollisionEvent(world, entity, collider, Collision.tile(collision.getOtherBounds()));
        } else {
            fireCollisionEvent(world, entity, collider, Collision.entity(collision.getOther(), collision.getOtherBounds()));
            fireCollisionEvent(world, collision.getOther(), collision.getOtherCollider(), Collision.entity(entity, newTargetBounds));
        }
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

    private void gatherOverlappingTileBounds(
            @NonNull final List<TileMap<TileType>> tileMapLayers,
            @NonNull final Rectangled oldBounds,
            @NonNull final Rectangled targetBounds,
            @NonNull final List<CollisionCandidate> collisions
    ) {
        var startX = (int) Math.floor(targetBounds.minX);
        var startY = (int) Math.floor(targetBounds.minY);
        var endX = (int) Math.ceil(targetBounds.maxX);
        var endY = (int) Math.ceil(targetBounds.maxY);

        startX = Math.min(startX, (int) Math.floor(oldBounds.minX));
        startY = Math.min(startY, (int) Math.floor(oldBounds.minY));
        endX = Math.max(endX, (int) Math.ceil(oldBounds.maxX));
        endY = Math.max(endY, (int) Math.ceil(oldBounds.maxY));

        val width = endX - startX;
        val height = endY - startY;

        GenerateStream.ofCoordinates(startX, startY, width, height)
                      .filter(pos -> tileMapLayers.stream()
                                                  .map(tm -> tm.getTile(pos))
                                                  .anyMatch(TileType::isSolid))
                      .map(pos -> new Rectangled(pos.x, pos.y, pos.x + 1, pos.y + 1))
                      .filter(targetBounds::intersects)
                      .map(CollisionCandidate::new)
                      .forEach(collisions::add);
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
