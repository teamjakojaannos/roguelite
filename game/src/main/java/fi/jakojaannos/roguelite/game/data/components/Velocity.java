package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import org.joml.Vector2d;

public class Velocity implements Component {
    public Vector2d velocity = new Vector2d(0.0f, 0.0f);
}
