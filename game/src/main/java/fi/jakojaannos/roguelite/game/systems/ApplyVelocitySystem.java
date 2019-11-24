package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.engine.utilities.GenerateStream;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.val;
import org.joml.Rectangled;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplyVelocitySystem implements ECSSystem {


    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return List.of(Transform.class, Velocity.class);
    }

    private final Vector2d tmpVelocity = new Vector2d();
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

            var targetBounds = calculateDestinationBounds(delta, transform, velocity);
            targetBounds = updateTargetBoundsBasedOnCollisions(world, entitiesWithCollider, tileMapLayers, entity, transform, velocity, targetBounds);

            applyMovement(transform, targetBounds);
        });
    }

    private Rectangled calculateDestinationBounds(
            double delta,
            Transform transform,
            Velocity velocity
    ) {
        velocity.velocity.mul(delta, tmpVelocity);
        return transform.bounds.translate(tmpVelocity, tmpTargetBounds);
    }

    private void applyMovement(Transform transform, Rectangled targetBounds) {
        transform.bounds.minX = targetBounds.minX;
        transform.bounds.minY = targetBounds.minY;
        transform.bounds.maxX = targetBounds.maxX;
        transform.bounds.maxY = targetBounds.maxY;
    }

    private Rectangled updateTargetBoundsBasedOnCollisions(
            @NonNull World world,
            List<Entity> entitiesWithCollider,
            List<TileMap<TileType>> tileMapLayers,
            Entity entity,
            Transform transform,
            Velocity velocity,
            Rectangled targetBounds
    ) {
        if (world.getEntities().hasComponent(entity, Collider.class)) {
            val collider = world.getEntities().getComponentOf(entity, Collider.class).get();
            val overlappingSolidBounds = new ArrayList<Rectangled>();

            // TODO: Now, we are checking only coordinates we would end up after moving all
            //  the way to the targetBounds. Stretch the collider and do more refined checks
            //  to catch more collision cases with fast-moving entities.
            handleCollidingEntities(world, entitiesWithCollider, entity, transform, collider, overlappingSolidBounds);
            gatherOverlappingTileBounds(tileMapLayers, transform.bounds, targetBounds, overlappingSolidBounds);

            if (!overlappingSolidBounds.isEmpty()) {
                targetBounds = moveUntilCollision(transform, velocity, overlappingSolidBounds);
            }
        }
        return targetBounds;
    }

    private Rectangled moveUntilCollision(
            Transform transform,
            Velocity velocity,
            ArrayList<Rectangled> overlappingSolidBounds
    ) {
        Rectangled targetBounds;
        val stepSize = 0.01;
        val maxDistance = velocity.velocity.length();
        var steps = 0;
        while (steps * stepSize < maxDistance) {
            val testVelocity = velocity.velocity.normalize((steps + 1) * stepSize, tmpVelocity);
            val newTargetBounds = transform.bounds.translate(testVelocity, tmpTestTargetBounds);

            if (overlappingSolidBounds.stream().anyMatch(other -> other.intersects(newTargetBounds))) {
                break;
            }
            ++steps;
        }

        val testVelocity = velocity.velocity.normalize(steps * stepSize, tmpVelocity);
        targetBounds = transform.bounds.translate(testVelocity, tmpTargetBounds);
        return targetBounds;
    }

    private void handleCollidingEntities(
            @NonNull World world,
            List<Entity> entitiesWithCollider,
            Entity entity,
            Transform transform,
            Collider collider,
            ArrayList<Rectangled> overlappingSolidBounds
    ) {
        for (val other : entitiesWithCollider) {
            if (other.getId() == entity.getId()) {
                continue;
            }

            val otherCollider = world.getEntities().getComponentOf(other, Collider.class).get();
            val otherTransform = world.getEntities().getComponentOf(other, Transform.class).get();

            val intersects = otherTransform.bounds.intersects(transform.bounds);
            if (intersects) {
                // Solid entities prevent movement
                val shouldCollide = otherCollider.isSolidTo(entity, collider);
                if (shouldCollide) {
                    overlappingSolidBounds.add(otherTransform.bounds);
                }
                otherCollider.collisions.add(new CollisionEvent(entity));
                collider.collisions.add(new CollisionEvent(other));

                if (!world.getEntities().hasComponent(entity, RecentCollisionTag.class)) {
                    world.getEntities().addComponentTo(entity, new RecentCollisionTag());
                }
                if (!world.getEntities().hasComponent(other, RecentCollisionTag.class)) {
                    world.getEntities().addComponentTo(other, new RecentCollisionTag());
                }
            }
        }
    }

    private void gatherOverlappingTileBounds(
            List<TileMap<TileType>> tileMapLayers,
            Rectangled oldBounds,
            Rectangled targetBounds,
            ArrayList<Rectangled> overlappingSolidBounds
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
                      .forEach(overlappingSolidBounds::add);
    }
}
