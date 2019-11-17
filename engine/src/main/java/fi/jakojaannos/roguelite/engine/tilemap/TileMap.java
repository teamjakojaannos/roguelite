package fi.jakojaannos.roguelite.engine.tilemap;

import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public class TileMap<TTile> {
    @Getter private final int width;
    @Getter private final int height;
    @Getter private final int[] level;

    private final Map<Integer, TTile> tileTypeLookup = new HashMap<>();

    public TileMap(int width, int height, @NonNull TTile[] level) {
        if (level.length < width * height) {
            throw new IllegalArgumentException("Could not construct TileMap: Level did not contain enough tiles!");
        }

        this.width = width;
        this.height = height;
        this.level = new int[width * height];
    }
}
