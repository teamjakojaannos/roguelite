package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.engine.utilities.GenerateStream;
import fi.jakojaannos.roguelite.game.data.Collision;
import fi.jakojaannos.roguelite.game.data.CollisionEvent;
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


            var targetBounds = calculateDestinationBounds(delta, transform, velocity);
            if (world.getEntities().hasComponent(entity, Collider.class)) {
                val collider = world.getEntities().getComponentOf(entity, Collider.class).get();
                targetBounds = moveWithCollisionHandling(world, entitiesWithCollider, tileMapLayers, entity, transform, velocity, collider, targetBounds);
            }

            moveTo(transform, targetBounds);
        });
    }

    private void moveTo(Transform transform, Rectangled targetBounds) {
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
        val collisions = new ArrayList<Collision>();

        // TODO: Now, we are checking only coordinates we would end up after moving all
        //  the way to the targetBounds. Stretch the collider and do more refined checks
        //  to catch more collision cases with fast-moving entities.
        gatherCollidingEntities(world, entitiesWithCollider, entity, transform, collider, collisions);
        gatherOverlappingTileBounds(tileMapLayers, transform.bounds, targetBounds, collisions);

        if (!collisions.isEmpty()) {
            targetBounds = moveUntilCollision(world,
                                              entity,
                                              collider,
                                              transform.bounds,
                                              velocity.velocity,
                                              collisions,
                                              targetBounds);
        }

        return targetBounds;
    }

    private Rectangled calculateDestinationBounds(
            double delta,
            Transform transform,
            Velocity velocity
    ) {
        velocity.velocity.mul(delta, tmpVelocity);
        return transform.bounds.translate(tmpVelocity, tmpTargetBounds);
    }

    private Rectangled moveUntilCollision(
            World world,
            Entity entity,
            Collider collider,
            Rectangled initialBounds,
            Vector2d velocity,
            List<Collision> collisions,
            Rectangled targetBounds
    ) {
        val stepSize = 0.01;
        val maxDistance = velocity.length();
        var steps = 0;
        while (steps * stepSize < maxDistance) {
            val testVelocity = velocity.normalize((steps + 1) * stepSize, tmpVelocity);
            val newTargetBounds = initialBounds.translate(testVelocity, tmpTestTargetBounds);

            val intersecting = collisions.stream()
                                         .filter(other -> other.getBounds().intersects(newTargetBounds))
                                         .findFirst();
            if (intersecting.isPresent()) {
                val collision = intersecting.get();
                if (collision.getType() == Collision.Type.ENTITY) {
                    val entityCollision = collision.getAsEntityCollision();
                    val other = entityCollision.getOther();
                    val otherCollider = world.getEntities().getComponentOf(other, Collider.class).get();
                    otherCollider.collisions.add(new CollisionEvent(collision));
                    collider.collisions.add(new CollisionEvent(Collision.entity(entity, newTargetBounds)));

                    if (!world.getEntities().hasComponent(entity, RecentCollisionTag.class)) {
                        world.getEntities().addComponentTo(entity, new RecentCollisionTag());
                    }
                    if (!world.getEntities().hasComponent(other, RecentCollisionTag.class)) {
                        world.getEntities().addComponentTo(other, new RecentCollisionTag());
                    }
                } else {
                    collider.collisions.add(new CollisionEvent(Collision.tile(collision.getBounds())));

                    if (!world.getEntities().hasComponent(entity, RecentCollisionTag.class)) {
                        world.getEntities().addComponentTo(entity, new RecentCollisionTag());
                    }
                }
                break;
            }
            ++steps;
        }

        val actualVelocity = velocity.normalize(steps * stepSize, tmpVelocity);
        return initialBounds.translate(actualVelocity, targetBounds);
    }

    private void gatherCollidingEntities(
            @NonNull World world,
            List<Entity> entitiesWithCollider,
            Entity entity,
            Transform transform,
            Collider collider,
            List<Collision> collisions
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
                    collisions.add(Collision.entity(other, otherTransform.bounds));
                }
                // FIXME: Handle these AFTER we know the final movement, now these can trigger
                //  false-positives
                else {
                    otherCollider.collisions.add(new CollisionEvent(Collision.entity(entity, transform.bounds)));
                    collider.collisions.add(new CollisionEvent(Collision.entity(other, otherTransform.bounds)));

                    if (!world.getEntities().hasComponent(entity, RecentCollisionTag.class)) {
                        world.getEntities().addComponentTo(entity, new RecentCollisionTag());
                    }
                    if (!world.getEntities().hasComponent(other, RecentCollisionTag.class)) {
                        world.getEntities().addComponentTo(other, new RecentCollisionTag());
                    }
                }
            }
        }
    }

    private void gatherOverlappingTileBounds(
            List<TileMap<TileType>> tileMapLayers,
            Rectangled oldBounds,
            Rectangled targetBounds,
            List<Collision> collisions
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
                      .forEach(bounds -> collisions.add(Collision.tile(bounds)));
    }

}
