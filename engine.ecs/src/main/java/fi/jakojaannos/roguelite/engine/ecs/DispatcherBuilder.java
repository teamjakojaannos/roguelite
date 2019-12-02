package fi.jakojaannos.roguelite.engine.ecs;

import lombok.NonNull;
import lombok.val;

public interface DispatcherBuilder {
    DispatcherBuilder withGroup(@NonNull SystemGroup group);

    default DispatcherBuilder withGroups(SystemGroup... groups) {
        for (val group : groups) {
            withGroup(group);
        }
        return this;
    }

    DispatcherBuilder addGroupDependency(
            @NonNull SystemGroup group,
            @NonNull SystemGroup dependency
    );

    default DispatcherBuilder addGroupDependencies(
            @NonNull SystemGroup group,
            @NonNull SystemGroup... dependencies
    ) {
        for (val dependency : dependencies) {
            addGroupDependency(group, dependency);
        }
        return this;
    }

    DispatcherBuilder withSystem(@NonNull ECSSystem system);

    SystemDispatcher build();
}
