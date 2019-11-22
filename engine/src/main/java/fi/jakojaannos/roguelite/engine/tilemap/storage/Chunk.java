package fi.jakojaannos.roguelite.engine.tilemap.storage;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Chunk<TTile> {
    public static final int CHUNK_SIZE = 16;
    private static final int TILES_PER_CHUNK = CHUNK_SIZE * CHUNK_SIZE;

    private final int[] tileData = new int[TILES_PER_CHUNK];
    @Getter private final int chunkX, chunkY;

    private transient final List<TTile> tileTypes = new ArrayList<>();
    private transient final Map<TTile, Integer> tileTypeToIndex = new HashMap<>();

    public Chunk(final int chunkX, final int chunkY, @NonNull final TTile defaultTileType) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.tileTypes.add(defaultTileType);
        this.tileTypeToIndex.put(defaultTileType, 0);
    }

    private int local(int coord) {
        return Math.abs(coord % CHUNK_SIZE);
    }

    public TTile getByTileCoordinates(
            final int x,
            final int y
    ) {
        return getByLocal(local(x), local(y));
    }

    public void setByTileCoordinates(
            final int x,
            final int y,
            @NonNull final TTile tileType
    ) {
        setByLocal(local(x), local(y), tileType);
    }

    private int index(final int x, final int y) {
        return y * CHUNK_SIZE + x;
    }

    private TTile getByLocal(final int localX, final int localY) {
        return this.tileTypes.get(this.tileData[index(localX, localY)]);
    }

    private void setByLocal(final int localX, final int localY, @NonNull final TTile tileType) {
        this.tileData[index(localX, localY)] = resolveTileTypeIndex(tileType);
    }

    private int resolveTileTypeIndex(@NonNull final TTile tileType) {
        return this.tileTypeToIndex.computeIfAbsent(tileType,
                                                    type -> {
                                                        val index = this.tileTypes.size();
                                                        this.tileTypes.add(type);
                                                        return index;
                                                    });
    }
}
