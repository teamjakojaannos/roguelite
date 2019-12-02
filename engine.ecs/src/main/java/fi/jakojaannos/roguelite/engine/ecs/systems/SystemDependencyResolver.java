package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

class SystemDependencyResolver {
    private final Map<Class<? extends ECSSystem>, Entry> systems = new HashMap<>();
    private final Map<SystemGroup, InternalSystemGroup.Builder> groups = new HashMap<>();

    private boolean firstInstanceAdded = false;

    void addGroup(@NonNull final SystemGroup group) {
        if (this.firstInstanceAdded) {
            throw new IllegalStateException("System groups must be registered before any systems are registered!");
        }

        this.groups.put(group, InternalSystemGroup.builder()
                                                  .group(group));
    }

    void addInstance(@NonNull final ECSSystem instance) {
        this.firstInstanceAdded = true;
        entryFor(instance);
    }

    void tickAfter(
            @NonNull final ECSSystem instance,
            @NonNull final Class<? extends ECSSystem> other
    ) {
        if (other.equals(instance.getClass())) {
            throw new IllegalStateException("System cannot depend on itself!");
        }

        entryFor(instance).tickAfter.add(other);
        entryFor(other).tickBefore.add(instance.getClass());
    }

    void tickBefore(
            @NonNull final ECSSystem instance,
            @NonNull final Class<? extends ECSSystem> other
    ) {
        if (other.equals(instance.getClass())) {
            throw new IllegalStateException("System cannot depend on itself!");
        }

        entryFor(instance).tickBefore.add(other);
        entryFor(other).tickAfter.add(instance.getClass());
    }

    void tickAfter(
            @NonNull final ECSSystem instance,
            @NonNull final SystemGroup group
    ) {
        entryFor(instance).tickAfterGroup.add(group);
    }

    void tickBefore(
            @NonNull final ECSSystem instance,
            @NonNull final SystemGroup group
    ) {
        entryFor(instance).tickBeforeGroup.add(group);
        this.groups.get(group).dependency(instance.getClass());
    }

    private Entry entryFor(Class<? extends ECSSystem> systemClass) {
        return this.systems.computeIfAbsent(systemClass, clazz -> new Entry());
    }

    private Entry entryFor(ECSSystem systemInstance) {
        val entry = entryFor(systemInstance.getClass());
        if (entry.systemInstance == null) {
            entry.systemInstance = systemInstance;
        } else if (entry.systemInstance != systemInstance) {
            throw new IllegalStateException("Multiple instances registered for the same system!");
        }

        return entry;
    }

    void addToGroup(
            @NonNull final SystemGroup group,
            @NonNull final ECSSystem instance
    ) {
        if (!this.groups.containsKey(group)) {
            throw new IllegalArgumentException(String.format("Unknown group \"%s\"!", group.getName()));
        }
        this.groups.get(group).system(instance);
    }

    public SystemDependencies buildFor(ECSSystem instance) {
        val entry = this.systems.get(instance.getClass());

        validateSystemInstances(entry.tickAfter);
        validateSystemInstances(entry.tickBefore);

        entry.tickBeforeGroup.forEach(group -> this.groups.get(group).dependency(instance.getClass()));
        return new SystemDependencies(entry.tickAfter, entry.tickAfterGroup);
    }

    private void validateSystemInstances(Collection<Class<? extends ECSSystem>> systems) {
        if (!systems.stream()
                    .map(this::entryFor)
                    .allMatch(Entry::hasInstance)) {
            throw new IllegalStateException(String.format(
                    "Non-registered systems (%s) found on the dependency graph!",
                    systems.stream()
                           .filter(dependency -> !entryFor(dependency).hasInstance())
                           .collect(Collectors.toList())
            ));
        }
    }

    public List<InternalSystemGroup> buildGroups() {
        val groups = this.groups.values()
                                .stream()
                                .map(InternalSystemGroup.Builder::build)
                                .collect(Collectors.toList());
        for (val group : groups) {
            for (val dependency : (Iterable<Class<? extends ECSSystem>>) group.getDependencies()::iterator) {
                if (group.getSystems().anyMatch(system -> system.equals(dependency))) {
                    throw new IllegalStateException(String.format(
                            "Group %s depends on the system %s which belongs to it!",
                            group.getGroup().getName(),
                            dependency.getSimpleName()));
                }
            }

            for (val system : (Iterable<Class<? extends ECSSystem>>) group.getSystems()::iterator) {
                if (entryFor(system).tickAfterGroup.contains(group.getGroup())) {
                    throw new IllegalStateException(String.format(
                            "System %s depends on the group %s which it belongs to!",
                            system.getSimpleName(),
                            group.getGroup().getName()));
                }
            }
        }

        return groups;
    }

    public void addGroupDependency(SystemGroup group, SystemGroup dependency) {
        this.groups.get(group).groupDependency(dependency);
    }

    @RequiredArgsConstructor
    private static class Entry {
        private final List<Class<? extends ECSSystem>> tickAfter = new ArrayList<>();
        private final List<Class<? extends ECSSystem>> tickBefore = new ArrayList<>();
        private final List<SystemGroup> tickAfterGroup = new ArrayList<>();
        private final List<SystemGroup> tickBeforeGroup = new ArrayList<>();
        private ECSSystem systemInstance;

        public boolean hasInstance() {
            return this.systemInstance != null;
        }
    }
}
