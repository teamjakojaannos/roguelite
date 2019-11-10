package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.engine.utilities.BitMaskUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

class SystemMap {
    private final Map<String, Integer> systemIdLookup = new HashMap<>();
    private final List<Entry> systemsById = new ArrayList<>();
    @NonNull
    private final Cluster cluster;

    private boolean dirty;

    SystemMap(Cluster cluster) {
        this.cluster = cluster;
    }

    void put(
            @NonNull String name,
            @NonNull ECSSystem system,
            @NonNull String... dependencies
    ) {
        val id = this.systemsById.size();
        val requiredComponentsBitMask =
                system.getRequiredComponents()
                      .stream()
                      .map(this.cluster::getComponentTypeIndexFor)
                      .filter(Optional::isPresent)
                      .map(Optional::get)
                      .reduce(new byte[BitMaskUtils.calculateMaskSize(cluster.getNumberOfComponentTypes())],
                              BitMaskUtils::setNthBit,
                              BitMaskUtils::combineMasks);

        val entry = new Entry(id,
                              system,
                              Arrays.stream(dependencies)
                                    .mapToInt(this.systemIdLookup::get)
                                    .toArray(),
                              requiredComponentsBitMask);
        this.systemsById.add(entry);
        this.systemIdLookup.put(name, id);

        this.dirty = true;
    }

    void forEachPrioritized(@NonNull BiConsumer<ECSSystem, byte[]> forEach) {
        refreshPriorityListIfDirty();
        this.systemsById.forEach(entry -> forEach.accept(entry.system, entry.requiredComponentBitMask));
    }

    private void refreshPriorityListIfDirty() {
        if (!this.dirty) {
            return;
        }

        List<Entry> roots = new ArrayList<>();

    }

    Stream<ECSSystem> nonPrioritizedStream() {
        return this.systemsById.stream().map(entry -> entry.system);
    }

    @RequiredArgsConstructor
    private static class Entry {
        private final int systemId;
        private final ECSSystem system;
        private final int[] dependencies;
        private final byte[] requiredComponentBitMask;
    }
}
