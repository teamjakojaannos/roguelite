package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.NonNull;
import lombok.val;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CameraControlSystem implements ECSSystem {

    public static final List<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(Camera.class);

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
        entities.forEach(entity -> {
            val camera = world.getEntities().getComponentOf(entity, Camera.class).get();
            if (camera.followTarget != null) {
                world.getEntities()
                     .getComponentOf(camera.followTarget, Transform.class)
                     .ifPresent(transform -> camera.pos.set(transform.getCenterX(),
                                                            transform.getCenterY()));
            }
        });
    }
}
