package fi.jakojaannos.roguelite.engine.tilemap.storage;

import lombok.NonNull;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

public class ChunkMap<TTile> {
    @NonNull private final TTile defaultTile;
    @NonNull private Map<Long, Chunk<TTile>> chunks = new HashMap<>();

    public ChunkMap(@NonNull final TTile defaultTile) {
        this.defaultTile = defaultTile;
    }

    @NonNull
    public Chunk<TTile> getByTileCoordinates(final int x, final int y) {
        val chunkX = Math.floorDiv(x, Chunk.CHUNK_SIZE);
        val chunkY = Math.floorDiv(y, Chunk.CHUNK_SIZE);
        return getByChunkCoordinates(chunkX, chunkY);
    }

    private static long packCoordinate(final int x, final int y) {
        return (((long) x) << 32) | (y & 0xffffffffL);
    }

    public Chunk<TTile> getByChunkCoordinates(final int chunkX, final int chunkY) {
        return this.chunks.computeIfAbsent(packCoordinate(chunkX, chunkY),
                                           key -> new Chunk<>(chunkX, chunkY, defaultTile));
    }
}
