package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;

public class Level implements Component {
    public TileMap<TileType> tileMap;

    public Level(TileMap<TileType> tileMap) {
        this.tileMap = tileMap;
    }
}
