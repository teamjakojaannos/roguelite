package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DispatchContext {
    private final Map<SystemGroup, InternalSystemGroup> groups = new HashMap<>();
    private final Map<Class<? extends ECSSystem>, Boolean> dispatchStatus = new HashMap<>();

    public DispatchContext(
            @NonNull final Collection<SystemContext> systemContexts,
            @NonNull final Collection<InternalSystemGroup> systemGroups
    ) {
        systemContexts.forEach(ctx -> this.dispatchStatus.put(ctx.getInstance().getClass(), false));
        systemGroups.forEach(internalGroup -> this.groups.put(internalGroup.getGroup(), internalGroup));
    }

    public boolean isReadyToDispatch(@NonNull final SystemContext systemContext) {
        return systemContext.getDependencies()
                            .stream()
                            .allMatch(this::isDispatched)
                && systemContext.getDependencies()
                                .groupDependenciesAsStream()
                                .allMatch(this::isDispatched)
                && systemContext.getGroups()
                                .allMatch(this::isReadyToDispatch);
    }

    public boolean isReadyToDispatch(@NonNull final SystemGroup group) {
        val internalGroup = this.groups.get(group);
        return internalGroup.getDependencies()
                            .allMatch(this::isDispatched)
                && internalGroup.getGroupDependencies()
                                .allMatch(this::isDispatched);
    }

    public boolean notDispatched(@NonNull final Class<? extends ECSSystem> system) {
        return !isDispatched(system);
    }

    public boolean notDispatched(@NonNull final SystemGroup group) {
        return !isDispatched(group);
    }

    public boolean isDispatched(@NonNull final Class<? extends ECSSystem> system) {
        return this.dispatchStatus.get(system);
    }

    public boolean isDispatched(@NonNull final SystemGroup group) {
        return this.groups.get(group)
                          .getSystems()
                          .allMatch(this::isDispatched);
    }

    @NonNull
    public Optional<Class<? extends ECSSystem>> findAnyNotDispatched() {
        for (val entry : this.dispatchStatus.entrySet()) {
            if (!entry.getValue()) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    public void setDispatched(final @NonNull SystemContext system) {
        this.dispatchStatus.put(system.getInstance().getClass(), true);
    }
}
