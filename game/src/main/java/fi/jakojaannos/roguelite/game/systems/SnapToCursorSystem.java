package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.CrosshairTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import lombok.NonNull;
import lombok.val;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SnapToCursorSystem implements ECSSystem {
    private static final List<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Transform.class, CrosshairTag.class
    );

    private static final List<Class<? extends Resource>> REQUIRED_RESOURCES = List.of(
            Mouse.class, CameraProperties.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    @Override
    public Collection<Class<? extends Resource>> getRequiredResources() {
        return REQUIRED_RESOURCES;
    }

    private final Vector2d tmpCamPos = new Vector2d();

    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        val mouse = world.getResource(Mouse.class);
        val camProps = world.getResource(CameraProperties.class);

        val cursorPosition = Optional.ofNullable(camProps.cameraEntity)
                                     .map(e -> world.getEntityManager().getComponentOf(e, Camera.class))
                                     .filter(Optional::isPresent)
                                     .map(Optional::get)
                                     .map(cam -> mouse.calculateCursorPositionRelativeToCamera(cam, camProps, tmpCamPos))
                                     .orElseGet(() -> tmpCamPos.set(0.0, 0.0));

        entities.forEach(entity -> {
            val transform = world.getEntityManager().getComponentOf(entity, Transform.class).get();
            transform.setPosition(cursorPosition.x, cursorPosition.y);
        });
    }
}
