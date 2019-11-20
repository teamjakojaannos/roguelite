package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;

public class Level implements Component {
    public TileMap<Integer> tileMap;

    public Level(TileMap<Integer> tileMap) {
        this.tileMap = tileMap;
    }
}
