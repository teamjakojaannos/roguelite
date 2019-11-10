package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.engine.utilities.BitMaskUtils;
import fi.jakojäännös.roguelite.game.data.GameState;
import lombok.NonNull;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

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

        this.systems.forEachPrioritized(
                (system, requiredComponentBitMask) ->
                        system.tick(this.cluster.getEntityStorage()
                                                .stream()
                                                .filter(entity -> BitMaskUtils.compareMasks(entity.getComponentBitmask(),
                                                                                            requiredComponentBitMask)),
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
