package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import lombok.NonNull;

import java.util.Queue;

public interface Game<TState> extends AutoCloseable {
    @NonNull
    TimeManager getTime();

    boolean isFinished();

    void setFinished(boolean state);

    boolean isDisposed();

    void tick(@NonNull TState state, @NonNull Queue<InputEvent> inputEvents, double delta);
}
