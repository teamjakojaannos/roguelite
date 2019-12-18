package fi.jakojaannos.roguelite.engine.state;

import fi.jakojaannos.roguelite.engine.ecs.World;

public interface WorldProvider {
    World getWorld();
}
