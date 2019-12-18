package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.utilities.UpdateableTimeManager;

import java.util.Queue;

public interface Game<TState> extends AutoCloseable {
    UpdateableTimeManager getTime();

    boolean isFinished();

    void setFinished(boolean state);

    boolean isDisposed();

    TState tick(TState state, Queue<InputEvent> inputEvents, double delta);
}
