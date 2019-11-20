package fi.jakojaannos.roguelite.engine.tilemap;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileMap<TTile> {
    @Getter private final int width;
    @Getter private final int height;
    @Getter private final int[] level;

    private transient final List<TTile> tileTypes = new ArrayList<>();
    private transient final Map<TTile, Integer> tileTypeToIndex = new HashMap<>();

    public TileMap(int width, int height, @NonNull TTile[] level) {
        if (level.length < width * height) {
            throw new IllegalArgumentException("Could not construct TileMap: Level did not contain enough tiles!");
        }

        this.width = width;
        this.height = height;
        this.level = new int[width * height];
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                val tileType = level[index(x, y)];
                var index = this.tileTypeToIndex.get(tileType);
                if (index == null) {
                    index = this.tileTypes.size();
                    this.tileTypes.add(tileType);
                    this.tileTypeToIndex.put(tileType, index);
                }
                this.level[index(x, y)] = index;
            }
        }
    }

    private int index(int x, int y) {
        return y * this.width + x;
    }
}
