package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

class SystemMap {
    private final Map<String, Integer> systemIdLookup = new HashMap<>();
    private final List<Entry> systemsById = new ArrayList<>();

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
            @NonNull ECSSystem system,
            String... dependencies
    ) {
        val id = getSystemCount();
        val entry = new Entry(id, system);

        Arrays.stream(dependencies)
              .forEach(dependencyName ->
                               entry.addDependency(Optional.ofNullable(this.systemIdLookup.get(dependencyName))
                                                           .map(this.systemsById::get)
                                                           .orElseThrow(() -> new IllegalStateException("Tried to register dependency \"" + dependencyName + "\" which was not registered!"))));

        this.systemsById.add(entry);
        this.systemIdLookup.put(name, id);
    }

    void forEachPrioritized(@NonNull Consumer<ECSSystem> forEach) {
        if (this.getSystemCount() == 0) {
            return;
        }

        boolean[] processed = new boolean[getSystemCount()];
        int nextEntryPoint;
        while ((nextEntryPoint = indexOfFistFalse(processed)) != -1) {
            Deque<Entry> queue = new ArrayDeque<>();
            queue.add(this.systemsById.get(nextEntryPoint));
            while (!queue.isEmpty()) {
                val entry = queue.getFirst();
                if (canBeProcessed(entry, processed)) {
                    queue.removeFirst();
                    forEach.accept(entry.system);
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

    Stream<ECSSystem> nonPrioritizedStream() {
        return this.systemsById.stream().map(entry -> entry.system);
    }

    @RequiredArgsConstructor
    private static class Entry {
        private final int id;
        private final ECSSystem system;
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
