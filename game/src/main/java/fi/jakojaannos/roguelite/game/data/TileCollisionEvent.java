package fi.jakojaannos.roguelite.game.data;

import lombok.RequiredArgsConstructor;
import org.joml.Vector2i;

@RequiredArgsConstructor
public class TileCollisionEvent {
    private final Vector2i pos;
}
