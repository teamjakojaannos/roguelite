package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class SystemDispatcherImpl implements SystemDispatcher {
    @NonNull private final SystemStorage systems;

    @Override
    public void dispatch(
            @NonNull final World world,
            final double delta
    ) {
        val entities = world.getEntityManager();
        this.systems.forEachPrioritized(
                (context) -> context.getInstance().tick(entities.getEntitiesWith(context.getRequirements().getRequiredComponents(),
                                                                                 context.getRequirements().getExcludedComponents(),
                                                                                 context.getRequirements().getRequiredGroups(),
                                                                                 context.getRequirements().getExcludedGroups()),
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
