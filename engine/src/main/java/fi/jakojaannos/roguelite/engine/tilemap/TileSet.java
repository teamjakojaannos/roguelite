package fi.jakojaannos.roguelite.engine.tilemap;

import java.util.List;

public class TileSet<TTexture> {
     private final TTexture texture;
    private final int columns, rows;
     private final List<TileType> tileTypes;

    public TileSet(
             TTexture texture,
            int columns,
            int rows,
             List<TileType> tileTypes
    ) {
        this.texture = texture;
        this.columns = columns;
        this.rows = rows;
        this.tileTypes = tileTypes;
    }
}
