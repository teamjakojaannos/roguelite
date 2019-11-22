package fi.jakojaannos.roguelite.game.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import org.joml.Vector2d;

public class Mouse implements Resource {
    public final Vector2d pos = new Vector2d(-999.0, -999.0);

    public final Vector2d calculateCursorPositionRelativeToCamera(Camera camera, CameraProperties camProps, Vector2d result) {
        return result.set(camera.pos.x + this.pos.x * camProps.viewportWidthInWorldUnits - camProps.viewportWidthInWorldUnits / 2.0,
                          camera.pos.y + this.pos.y * camProps.viewportHeightInWorldUnits - camProps.viewportHeightInWorldUnits / 2.0);
    }
}
