package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import org.joml.Vector2d;

public class CharacterAbilities implements Component {
    public double attackTimer;
    public Vector2d attackTarget = new Vector2d();
}
