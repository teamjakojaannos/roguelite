package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.engine.utilities.BitMaskUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

class SystemMap<TState> {
    private final Map<String, Integer> systemIdLookup = new HashMap<>();
    private final List<Entry> systemsById = new ArrayList<>();
    @NonNull
    private final Function<Class<? extends Component>, Optional<Integer>> componentTypeMapper;
    private final int nComponentTypes;

    SystemMap(
            @NonNull Function<Class<? extends Component>, Optional<Integer>> componentTypeMapper,
            int nComponentTypes
    ) {
        this.componentTypeMapper = componentTypeMapper;
        this.nComponentTypes = nComponentTypes;
    }

    private int getSystemCount() {
        return this.systemsById.size();
    }

    Stream<ECSSystem> getDependencies(String name) {
        return Stream.ofNullable(this.systemIdLookup.get(name))
                     .map(this.systemsById::get)
                     .flatMap(entry -> entry.dependencies.stream())
                     .map(entry -> entry.system);
    }

    void put(
            @NonNull String name,
            @NonNull ECSSystem<TState> system,
            String... dependencies
    ) {
        val id = getSystemCount();
        val requiredComponentsBitMask =
                system.getRequiredComponents()
                      .stream()
                      .map(this.componentTypeMapper)
                      .filter(Optional::isPresent)
                      .map(Optional::get)
                      .reduce(new byte[BitMaskUtils.calculateMaskSize(this.nComponentTypes)],
                              BitMaskUtils::setNthBit,
                              BitMaskUtils::combineMasks);

        val entry = new Entry(id, system, requiredComponentsBitMask);

        Arrays.stream(dependencies)
              .forEach(dependencyName ->
                               entry.addDependency(Optional.ofNullable(this.systemIdLookup.get(dependencyName))
                                                           .map(this.systemsById::get)
                                                           .orElseThrow(() -> new IllegalStateException("Tried to register dependency \"" + dependencyName + "\" which was not registered!"))));

        this.systemsById.add(entry);
        this.systemIdLookup.put(name, id);
    }

    void forEachPrioritized(@NonNull BiConsumer<ECSSystem<TState>, byte[]> forEach) {
        if (this.getSystemCount() == 0) {
            return;
        }

        boolean[] processed = new boolean[getSystemCount()];
        int nextEntryPoint = -1;
        while ((nextEntryPoint = indexOfFistFalse(processed)) != -1) {
            Deque<Entry> queue = new ArrayDeque<>();
            queue.add(this.systemsById.get(nextEntryPoint));
            while (!queue.isEmpty()) {
                val entry = queue.getFirst();
                if (canBeProcessed(entry, processed)) {
                    queue.removeFirst();
                    forEach.accept(entry.system, entry.requiredComponentBitMask);
                    processed[entry.id] = true;
                } else {
                    entry.dependencies.stream()
                                      .filter(dep -> !processed[dep.id])
                                      .forEach(queue::addFirst);
                }
            }
        }
    }

    private boolean canBeProcessed(@NonNull Entry entry, @NonNull boolean[] processed) {
        return entry.hasNoDependencies() || entry.dependencies.stream()
                                                              .allMatch(dep -> processed[dep.id]);
    }

    private int indexOfFistFalse(boolean[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (!array[i]) {
                return i;
            }
        }

        return -1;
    }

    Stream<ECSSystem<TState>> nonPrioritizedStream() {
        return this.systemsById.stream().map(entry -> entry.system);
    }

    @RequiredArgsConstructor
    private class Entry {
        private final int id;
        private final ECSSystem<TState> system;
        private final byte[] requiredComponentBitMask;
        private final List<Entry> dependencies = new ArrayList<>(0);
        private final List<Entry> dependents = new ArrayList<>(0);

        boolean hasNoDependencies() {
            return this.dependencies.isEmpty();
        }

        void addDependency(Entry dependency) {
            // TODO: Check for circular dependencies
            dependencies.add(dependency);
            dependency.dependents.add(this);
        }
    }
}
