package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import lombok.Getter;

public class TileMapLayer implements Component {
    @Getter public TileMap<TileType> tileMap;
    @Getter public boolean collisionEnabled = true;

    public TileMapLayer(TileMap<TileType> tileMap) {
        this.tileMap = tileMap;
    }
}
