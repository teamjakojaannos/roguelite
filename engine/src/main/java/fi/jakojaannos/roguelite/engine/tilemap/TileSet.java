package fi.jakojaannos.roguelite.engine.tilemap;

import lombok.NonNull;

import java.util.List;

public class TileSet<TTexture> {
    @NonNull private final TTexture texture;
    private final int columns, rows;
    @NonNull private final List<TileType> tileTypes;

    public TileSet(
            @NonNull TTexture texture,
            int columns,
            int rows,
            @NonNull List<TileType> tileTypes
    ) {
        this.texture = texture;
        this.columns = columns;
        this.rows = rows;
        this.tileTypes = tileTypes;
    }
}
