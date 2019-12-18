package fi.jakojaannos.roguelite.engine.state;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.utilities.UpdateableTimeManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GameState implements TimeProvider, WorldProvider, WritableTimeProvider {
    @Getter private final World world;
    private final UpdateableTimeManager time;

    @Override
    public TimeManager getTime() {
        return this.time;
    }

    @Override
    public void updateTime() {
        this.time.refresh();
    }
}
