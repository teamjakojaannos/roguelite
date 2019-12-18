package fi.jakojaannos.roguelite.engine;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class GameBase implements Game {
    private boolean disposed = false;
    private boolean finished = false;

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public void setFinished(final boolean state) {
        this.finished = state;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void close() {
        if (this.disposed) {
            LOG.error(".close() called more than once for a game instance!");
            return;
        }
        this.disposed = true;
    }
}
