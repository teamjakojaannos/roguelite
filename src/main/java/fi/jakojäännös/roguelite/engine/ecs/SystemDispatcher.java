package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.game.data.GameState;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SystemDispatcher {
    @NonNull private final SystemMap systems;
    @NonNull private final Cluster cluster;

    SystemDispatcher(Cluster cluster, Collection<DispatcherBuilder.SystemEntry> systems) {
        this.cluster = cluster;
        this.systems = new SystemMap(this.cluster);
        systems.forEach(entry -> this.systems.put(entry.getName(),
                                                  entry.getSystem(),
                                                  entry.getDependencies()));

        verifyClusterIsCompatible(this.cluster);
    }

    public void dispatch(
            @NonNull GameState state,
            double delta
    ) {

        this.systems.forEach(
                (system, requiredComponentBitMask) ->
                        system.tick(cluster.getEntityStorage().stream()
                                           .filter(entity -> entity.compareMask(requiredComponentBitMask)),
                                    state,
                                    delta
                        ));
    }

    /**
     * Checks that all of the component types required by this dispatcher's systems are registered
     * to the given cluster.
     *
     * @param cluster cluster to check against
     *
     * @throws IllegalStateException if any of the required component types is not registered
     */
    private void verifyClusterIsCompatible(@NonNull Cluster cluster) {
        val allRequiredComponentTypesAreRegistered =
                this.systems.stream()
                            .flatMap(s -> s.getRequiredComponents().stream())
                            .map(cluster::getComponentTypeIndexFor)
                            .allMatch(Optional::isPresent);
        if (!allRequiredComponentTypesAreRegistered) {
            val notRegistered = this.systems.stream()
                                            .flatMap(s -> s.getRequiredComponents().stream())
                                            .filter(c -> !cluster.getComponentTypeIndexFor(c).isPresent())
                                            .collect(Collectors.toList());
            throw new IllegalStateException(String.format(
                    "Dispatcher requires component types %s which are not registered to the cluster being processed!",
                    notRegistered.toString()
            ));
        }
    }

    private static byte[] setComponentBit(byte[] maskBytes, int typeIndex) {
        maskBytes[typeIndex / 8] |= (1 << (typeIndex % 8));
        return maskBytes;
    }

    private static byte[] combineMaskBytes(byte[] a, byte[] b) {
        for (int n = 0; n < Math.min(a.length, b.length); ++n) {
            a[n] |= b[n];
        }
        return a;
    }

    private static class SystemMap {
        private final Map<String, Integer> systemIdLookup = new HashMap<>();
        private final List<Entry> systemsById = new ArrayList<>();
        @NonNull
        private final Cluster cluster;

        private SystemMap(Cluster cluster) {
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
                          .reduce(new byte[calculateMaskSize(cluster.getNumberOfComponentTypes())],
                                  SystemDispatcher::setComponentBit,
                                  SystemDispatcher::combineMaskBytes);

            val entry = new Entry(id,
                                  system,
                                  Arrays.stream(dependencies)
                                        .mapToInt(this.systemIdLookup::get)
                                        .toArray(),
                                  requiredComponentsBitMask);
            this.systemsById.add(entry);
            this.systemIdLookup.put(name, id);
        }

        void forEach(@NonNull BiConsumer<ECSSystem, byte[]> forEach) {
            this.systemsById.forEach(entry -> forEach.accept(entry.system, entry.requiredComponentBitMask));
        }

        Stream<ECSSystem> stream() {
            return this.systemsById.stream().map(entry -> entry.system);
        }

        private static int calculateMaskSize(int typeIndex) {
            return typeIndex / 8 + ((typeIndex % 8 == 0) ? 0 : 1);
        }

        @RequiredArgsConstructor
        private static class Entry {
            private final int systemId;
            private final ECSSystem system;
            private final int[] dependencies;
            private final byte[] requiredComponentBitMask;
        }
    }
}
