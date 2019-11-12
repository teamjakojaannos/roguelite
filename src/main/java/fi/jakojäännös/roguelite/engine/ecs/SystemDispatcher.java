package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.engine.utilities.BitMaskUtils;
import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class SystemDispatcher<TState> {
    @NonNull private final SystemMap<TState> systems;

    SystemDispatcher(Collection<DispatcherBuilder.SystemEntry<TState>> systems) {
        this.systems = new SystemMap<>();
        systems.forEach(entry -> this.systems.put(entry.getName(),
                                                  entry.getSystem(),
                                                  entry.getDependencies()));
    }

    public void dispatch(
            @NonNull Cluster cluster,
            @NonNull TState state,
            double delta
    ) {
        verifyClusterIsCompatible(cluster);

        this.systems.forEachPrioritized(
                (system) -> {
                    val requiredComponentsBitMask =
                            system.getRequiredComponents()
                                  .stream()
                                  .map(cluster::getComponentTypeIndexFor)
                                  .filter(Optional::isPresent)
                                  .map(Optional::get)
                                  .reduce(new byte[BitMaskUtils.calculateMaskSize(cluster.getNumberOfComponentTypes())],
                                          BitMaskUtils::setNthBit,
                                          BitMaskUtils::combineMasks);

                    system.tick(cluster.getEntityStorage()
                                       .stream()
                                       .filter(entity -> BitMaskUtils.compareMasks(entity.getComponentBitmask(),
                                                                                   requiredComponentsBitMask)),
                                state,
                                delta,
                                cluster);
                }
        );
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
                this.systems.nonPrioritizedStream()
                            .flatMap(s -> s.getRequiredComponents().stream())
                            .map(cluster::getComponentTypeIndexFor)
                            .allMatch(Optional::isPresent);
        if (!allRequiredComponentTypesAreRegistered) {
            val notRegistered = this.systems.nonPrioritizedStream()
                                            .flatMap(s -> s.getRequiredComponents().stream())
                                            .filter(c -> !cluster.getComponentTypeIndexFor(c).isPresent())
                                            .collect(Collectors.toList());
            throw new IllegalStateException(String.format(
                    "Dispatcher requires component types %s which are not registered to the cluster being processed!",
                    notRegistered.toString()
            ));
        }
    }
}
