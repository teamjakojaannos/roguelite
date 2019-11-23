package fi.jakojaannos.roguelite.game.data;

import lombok.NonNull;
import org.joml.Rectangled;
import org.joml.Vector2i;

public class TileCollisionEvent {
    private final Vector2i pos = new Vector2i();

    public TileCollisionEvent(Vector2i pos) {
        this.pos.set(pos);
    }


    public Rectangled getBounds(final double tileSize, @NonNull final Rectangled result) {
        result.minX = pos.x * tileSize;
        result.minY = pos.y * tileSize;
        result.maxX = (pos.x + 1) * tileSize;
        result.maxY = (pos.y + 1) * tileSize;
        return result;
    }
}
