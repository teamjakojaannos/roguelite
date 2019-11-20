package fi.jakojaannos.roguelite.game.data;

import fi.jakojaannos.roguelite.engine.ecs.World;
import lombok.Getter;
import lombok.NonNull;

public class GameState {
    @NonNull
    @Getter private final World world;

    public GameState(@NonNull World world) {
        this.world = world;
    }
}
