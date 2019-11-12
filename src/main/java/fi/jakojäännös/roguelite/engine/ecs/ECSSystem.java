package fi.jakojäännös.roguelite.engine.ecs;

import java.util.Collection;
import java.util.stream.Stream;

public interface ECSSystem<TState> {
    Collection<Class<? extends Component>> getRequiredComponents();

    // TODO: This is sub-optimal performance-wise; components should be stored so that they can
    //  be efficiently passed here via some specialized parameter data-structure
    void tick(
            Stream<Entity> entities,
            TState state,
            double delta
    );
}
