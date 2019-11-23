package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.NonNull;
import lombok.val;
import org.joml.Rectangled;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CharacterToTileMapCollisionSystem implements ECSSystem {
    private static final List<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            RecentCollisionTag.class,
            Transform.class,
            Collider.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    @Override
    public void tick(
            @NonNull final Stream<Entity> entities,
            @NonNull final World world,
            double delta
    ) {
        entities.forEach(entity -> {
            val collider = world.getEntities().getComponentOf(entity, Collider.class).get();
            val transform = world.getEntities().getComponentOf(entity, Transform.class).get();

            Rectangled originalBounds = world.getEntities()
                                             .getComponentOf(entity, Physics.class)
                                             .map(physics -> physics.oldBounds)
                                             .orElse(new Rectangled(transform.bounds));

            collider.tileCollisions.stream()
                                   .map(event -> event.getBounds(1.0, new Rectangled()))
                                   .filter(tileBounds -> tileBounds.intersects(transform.bounds))
                                   .reduce(this::combineRectangles)
                                   .map(combinedBounds -> findPushOutVector(transform.bounds, combinedBounds, originalBounds))
                                   .ifPresent(transform.bounds::translate);
        });
    }

    @NonNull
    private Rectangled combineRectangles(Rectangled a, Rectangled b) {
        return new Rectangled(Math.min(a.minX, b.minX),
                              Math.min(a.minY, b.minY),
                              Math.max(a.maxX, b.maxX),
                              Math.max(a.maxY, b.maxY));
    }

    private Vector2d findPushOutVector(
            Rectangled targetBounds,
            Rectangled obstacleBounds,
            Rectangled originalBounds
    ) {
        // This can go wrong in so many ways, but as long as objects are not very thin or perfectly
        // aligned, this *works*
        val obstacleW = obstacleBounds.maxX - obstacleBounds.minX;
        val obstacleH = obstacleBounds.maxY - obstacleBounds.minY;

        double resultX, resultY;
        // TODO: We need a better heuristic for determining which way to go. Position delta
        //  calculated from target/original bounds does not seem to be reliable, either. Velocity
        //  field on physics or a separate component?
        if (obstacleH < obstacleW) {
            resultX = 0.0;
            val agentHeight = targetBounds.maxY - targetBounds.minY;
            val targetY = originalBounds.minY > targetBounds.minY
                    ? obstacleBounds.maxY
                    : obstacleBounds.minY - agentHeight;

            resultY = targetY - targetBounds.minY;
        } else {
            resultY = 0.0;
            val agentWidth = targetBounds.maxX - targetBounds.minX;
            val targetX = originalBounds.minX > targetBounds.minX
                    ? obstacleBounds.maxX
                    : obstacleBounds.minX - agentWidth;

            resultX = targetX - targetBounds.minX;
        }

        return new Vector2d(resultX, resultY);
    }
}
