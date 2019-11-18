package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
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
        this.systems.forEachPrioritized(
                (system) -> {
                    val requiredComponentsBitMask =
                            system.getRequiredComponents()
                                  .stream()
                                  .map(cluster::getComponentTypeIndexFor)
                                  .reduce(new byte[BitMaskUtils.calculateMaskSize(cluster.getMaxComponentTypes())],
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
