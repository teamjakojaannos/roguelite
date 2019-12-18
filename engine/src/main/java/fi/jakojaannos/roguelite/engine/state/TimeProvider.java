package fi.jakojaannos.roguelite.engine.state;

import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public interface TimeProvider extends Resource {
    TimeManager getTime();
}
