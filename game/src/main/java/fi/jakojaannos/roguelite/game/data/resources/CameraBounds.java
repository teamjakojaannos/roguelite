package fi.jakojaannos.roguelite.game.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Resource;

public class CameraBounds implements Resource {
    public double viewportWidthInWorldUnits;
    public double viewportHeightInWorldUnits;

    public double targetViewportSizeInWorldUnits = 24.0;
    public boolean targetViewportSizeRespectiveToMinorAxis = true;

    public CameraBounds() {

    }
}
