package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import org.joml.Vector2d;

public class SlimeAI implements Component {

    public double chaseRadiusSquared = 10.0 * 10.0,
            targetRadiusSquared = 1.0;

    public double airTime = 0.0,
            jumpCoolDown = 0.0;

    public Vector2d jumpDir = new Vector2d();

}
