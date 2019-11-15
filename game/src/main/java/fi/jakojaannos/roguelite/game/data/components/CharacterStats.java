package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.joml.Rectangled;

@NoArgsConstructor
@AllArgsConstructor
public class CharacterStats implements Component {
    public double speed = 4.0;
    public double acceleration = 1.0;
    public double friction = 2.0;

    public double attackRate = 2.0; // Attacks per second
    public double attackProjectileSpeed = 20.0;
}
