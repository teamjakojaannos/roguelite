package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.NoArgsConstructor;
import org.joml.Rectangled;

@NoArgsConstructor
public class Physics implements Component {
    public Rectangled oldBounds = new Rectangled();

    public Physics(Transform transform) {
        this.oldBounds.minX = transform.bounds.minX;
        this.oldBounds.minY = transform.bounds.minY;
        this.oldBounds.maxX = transform.bounds.maxX;
        this.oldBounds.maxY = transform.bounds.maxY;
    }
}
