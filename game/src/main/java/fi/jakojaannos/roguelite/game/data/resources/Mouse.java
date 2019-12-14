package fi.jakojaannos.roguelite.game.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import org.joml.Vector2d;

public class Mouse implements Resource {
    public final Vector2d pos = new Vector2d(Double.NaN, Double.NaN);

    public final Vector2d calculateCursorPositionRelativeToCamera(
            final Camera camera,
            final CameraProperties camProps,
            final Vector2d result
    ) {
        return result.set(camera.pos.x + this.pos.x * camProps.viewportWidthInWorldUnits - camProps.viewportWidthInWorldUnits / 2.0,
                          camera.pos.y + this.pos.y * camProps.viewportHeightInWorldUnits - camProps.viewportHeightInWorldUnits / 2.0);
    }
}
