package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.engine.utilities.GenerateStream;
import fi.jakojaannos.roguelite.game.data.TileCollisionEvent;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Rectangled;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates {@link CollisionEvent CollisionEvents} when entities with a {@link Collider} component
 * come in contact with solid tiles of a {@link TileMap}.
 */
@Slf4j
public class TileMapCollisionSystem implements ECSSystem {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Collider.class, Transform.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        // *shudders* ugly, but works
        val tileMapLayers = world.getEntities()
                                 .getEntitiesWith(TileMapLayer.class)
                                 .map(Entities.EntityComponentPair::getComponent)
                                 .filter(TileMapLayer::isCollisionEnabled)
                                 .map(TileMapLayer::getTileMap)
                                 .collect(Collectors.toList());

        entities.forEach(entity -> {
            val transform = world.getEntities().getComponentOf(entity, Transform.class).get();
            val collider = world.getEntities().getComponentOf(entity, Collider.class).get();

            var startX = (int) Math.floor(transform.bounds.minX);
            var startY = (int) Math.floor(transform.bounds.minY);
            var endX = (int) Math.ceil(transform.bounds.maxX);
            var endY = (int) Math.ceil(transform.bounds.maxY);

            val physicsComponent = world.getEntities().getComponentOf(entity, Physics.class);
            if (physicsComponent.isPresent()) {
                val physics = physicsComponent.get();
                startX = Math.min(startX, (int) Math.floor(physics.oldBounds.minX));
                startY = Math.min(startY, (int) Math.floor(physics.oldBounds.minY));
                endX = Math.max(endX, (int) Math.ceil(physics.oldBounds.maxX));
                endY = Math.max(endY, (int) Math.ceil(physics.oldBounds.maxY));
            }

            val width = endX - startX;
            val height = endY - startY;

            GenerateStream.ofCoordinates(startX, startY, width, height)
                          .filter(pos -> transform.bounds.intersects(new Rectangled(pos.x, pos.y, pos.x + 1, pos.y + 1)))
                          .filter(pos -> tileMapLayers.stream()
                                                      .map(tm -> tm.getTile(pos))
                                                      .anyMatch(TileType::isSolid))
                          .map(TileCollisionEvent::new)
                          .forEach(event -> {
                              if (!world.getEntities().hasComponent(entity, RecentCollisionTag.class)) {
                                  world.getEntities().addComponentTo(entity, new RecentCollisionTag());
                              }

                              collider.tileCollisions.add(event);
                          });
        });
    }
}
