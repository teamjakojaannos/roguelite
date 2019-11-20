package fi.jakojaannos.roguelite.engine.ecs;

import lombok.NonNull;

public interface SystemDispatcher extends AutoCloseable {
    void dispatch(
            @NonNull World world,
            double delta
    );
}
