package fi.jakojaannos.roguelite.game.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Resource;
import org.joml.Vector2d;

public class Mouse implements Resource {
    public final Vector2d pos = new Vector2d(-999.0, -999.0);
}
