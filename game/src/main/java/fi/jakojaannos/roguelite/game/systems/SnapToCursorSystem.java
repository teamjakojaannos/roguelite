package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.CrosshairTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.CameraBounds;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class SnapToCursorSystem implements ECSSystem {
    public static final List<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Transform.class, CrosshairTag.class
    );

    public static final List<Class<? extends Resource>> REQUIRED_RESOURCES = List.of(
            Mouse.class, CameraBounds.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    @Override
    public Collection<Class<? extends Resource>> getRequiredResources() {
        return REQUIRED_RESOURCES;
    }

    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        entities.forEach(entity -> {
            val transform = world.getEntities().getComponentOf(entity, Transform.class).get();
            val mouse = world.getResource(Mouse.class);
            val camBounds = world.getResource(CameraBounds.class);
            transform.setPosition(mouse.pos.x * camBounds.viewportWidthInWorldUnits,
                                  mouse.pos.y * camBounds.viewportHeightInWorldUnits);
        });
    }
}
