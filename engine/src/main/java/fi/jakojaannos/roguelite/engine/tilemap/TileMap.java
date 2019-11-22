package fi.jakojaannos.roguelite.engine.tilemap;

import fi.jakojaannos.roguelite.engine.tilemap.storage.ChunkMap;
import lombok.NonNull;
import lombok.val;
import org.joml.Vector2i;

public class TileMap<TTile> {
    private final ChunkMap<TTile> chunks;

    public TileMap(@NonNull final TTile defaultTile) {
        this.chunks = new ChunkMap<>(defaultTile);
    }

    public void setTile(@NonNull final Vector2i coordinates, @NonNull final TTile tileType) {
        setTile(coordinates.x, coordinates.y, tileType);
    }

    public void setTile(final int x, final int y, @NonNull final TTile tileType) {
        val chunk = this.chunks.getByTileCoordinates(x, y);
        chunk.setByTileCoordinates(x, y, tileType);
    }

    @NonNull
    public TTile getTile(@NonNull final Vector2i coordinates) {
        return getTile(coordinates.x, coordinates.y);
    }

    @NonNull
    public TTile getTile(final int x, final int y) {
        return this.chunks.getByTileCoordinates(x, y)
                          .getByTileCoordinates(x, y);
    }
}
