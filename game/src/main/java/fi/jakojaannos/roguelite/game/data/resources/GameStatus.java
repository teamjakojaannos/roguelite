package fi.jakojaannos.roguelite.game.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Resource;

public class GameStatus implements Resource {
    public boolean shouldRestart = false;
}
