package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.engine.utilities.BitMaskUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class SystemDispatcher<TState> implements AutoCloseable {
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
                                       .filter(entity -> BitMaskUtils.hasAllBitsOf(entity.getComponentBitmask(),
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

    @Override
    public void close() throws Exception {
        val exceptions = this.systems.nonPrioritizedStream()
                                     .filter(s -> s instanceof AutoCloseable)
                                     .map(s -> {
                                         try {
                                             ((AutoCloseable) s).close();
                                             return null;
                                         } catch (Exception e) {
                                             return e;
                                         }
                                     })
                                     .filter(Objects::nonNull)
                                     .toArray(Exception[]::new);

        if (exceptions.length > 0) {
            LOG.error("SYSTEM DISPATCHER FAILED TO DISPOSE ONE OR MORE SYSTEM(S)");
            throw new SystemDisposeException(exceptions);
        }
    }

    private static class SystemDisposeException extends Exception {
        SystemDisposeException(Exception[] exceptions) {
            super(Arrays.stream(exceptions)
                        .map(Exception::toString)
                        .reduce(new StringBuilder(),
                                (stringBuilder, s) -> stringBuilder.append(", ").append(s),
                                StringBuilder::append)
                        .toString());
        }
    }
}
