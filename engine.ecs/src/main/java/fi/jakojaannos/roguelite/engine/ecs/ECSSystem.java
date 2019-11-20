package fi.jakojaannos.roguelite.engine.ecs;

import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface ECSSystem {
    Collection<Class<? extends Resource>> EMPTY_REQUIRED_RESOURCES = List.of();
    Collection<Class<? extends Component>> EMPTY_REQUIRED_COMPONENTS = List.of();

    default Collection<Class<? extends Resource>> getRequiredResources() {
        return EMPTY_REQUIRED_RESOURCES;
    }

    default Collection<Class<? extends Component>> getRequiredComponents() {
        return EMPTY_REQUIRED_COMPONENTS;
    }

    void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    );
}
