package fi.jakojäännös.roguelite.game;

import fi.jakojäännös.roguelite.engine.Game;
import fi.jakojäännös.roguelite.engine.input.InputEvent;
import fi.jakojäännös.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojäännös.roguelite.engine.utilities.TimeManager;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Queue;

@Slf4j
public class Roguelite implements Game {
    private final SimpleTimeManager timeManager = new SimpleTimeManager();

    private boolean disposed = false;

    @NonNull
    @Override
    public TimeManager getTime() {
        return new SimpleTimeManager();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void setFinished(boolean state) {

    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void tick(Queue<InputEvent> inputEvents, double delta) {
        this.timeManager.tick();
        while (!inputEvents.isEmpty()) {
            val event = inputEvents.remove();
            LOG.info("Received input event: {}/{}:{}", event.getScancode(), event.getKey(), event.getAction());
        }
    }

    @Override
    public void close() throws Exception {
        if (this.disposed) {
            LOG.error(".close() called more than once for a game!");
            return;
        }
        this.disposed = true;
    }
}
