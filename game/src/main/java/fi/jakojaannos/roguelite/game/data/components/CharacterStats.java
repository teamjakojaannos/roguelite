package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class CharacterStats implements Component {
    public double speed = 4.0;
    public double acceleration = 1.0;
    public double friction = 2.0;
}
