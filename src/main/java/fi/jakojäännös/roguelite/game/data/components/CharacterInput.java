package fi.jakojäännös.roguelite.game.data.components;

import fi.jakojäännös.roguelite.engine.ecs.Component;
import org.joml.Vector2f;

public class CharacterInput implements Component {
    public Vector2f move = new Vector2f(0.0f, 0.0f);
}
