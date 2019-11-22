package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.storage.EntitiesImpl;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@Slf4j
public class SystemDispatcherImpl implements SystemDispatcher {
    @NonNull private final SystemMap systems;

    public SystemDispatcherImpl(Collection<DispatcherBuilder.SystemEntry> systems) {
        this.systems = new SystemMap();
        systems.forEach(entry -> this.systems.put(entry.getName(),
                                                  entry.getSystem(),
                                                  entry.getDependencies()));
    }

    @Override
    public void dispatch(
            @NonNull World world,
            double delta
    ) {
        val entities = (EntitiesImpl) world.getEntities();
        this.systems.forEachPrioritized(
                (system) -> system.tick(entities.getEntitiesWith(system.getRequiredComponents()),
                                        world,
                                        delta)
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
