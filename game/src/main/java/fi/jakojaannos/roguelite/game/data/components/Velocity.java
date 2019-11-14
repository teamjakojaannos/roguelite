package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import org.joml.Vector2f;

public class Velocity implements Component {
    public Vector2f velocity = new Vector2f(0.0f, 0.0f);
}
