package fi.jakojaannos.roguelite.engine.ecs;

import lombok.NonNull;

import java.util.Collection;
import java.util.stream.Stream;

public interface ECSSystem<TState> {
    Collection<Class<? extends Component>> getRequiredComponents();

    // TODO: This is sub-optimal performance-wise; components should be stored so that they can
    //  be efficiently passed here via some specialized parameter data-structure
    void tick(
            @NonNull Stream<Entity> entities,
            @NonNull TState state,
            double delta,
            @NonNull Cluster cluster
    );
}
