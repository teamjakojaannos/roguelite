package fi.jakojaannos.roguelite.game.data;

import fi.jakojaannos.roguelite.engine.ecs.World;
import lombok.Getter;

public class GameState {

    @Getter private final World world;

    public GameState( World world) {
        this.world = world;
    }
}
