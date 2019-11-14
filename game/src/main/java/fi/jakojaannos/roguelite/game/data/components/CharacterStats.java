package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class CharacterStats implements Component {
    public float speed = 4.0f;
    public float acceleration = 1.0f;
    public float friction = 2.0f;
}
