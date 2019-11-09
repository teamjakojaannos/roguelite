package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.game.data.GameState;
import lombok.NonNull;
import lombok.val;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class SystemDispatcher {
    private final Collection<ECSSystem> systems;

    public SystemDispatcher(Collection<ECSSystem> systems) {
        this.systems = systems;
    }

    public void dispatch(
            @NonNull Cluster cluster,
            GameState state,
            double delta
    ) {

        this.systems.forEach(system -> {
            val allRequiredComponentTypesAreRegistered = system.getRequiredComponents()
                                                               .stream()
                                                               .map(cluster::getComponentTypeIndexFor)
                                                               .allMatch(Optional::isPresent);
            if (!allRequiredComponentTypesAreRegistered) {
                val notRegistered = system.getRequiredComponents()
                                          .stream()
                                          .filter(c -> !cluster.getComponentTypeIndexFor(c).isPresent())
                                          .collect(Collectors.toList());
                throw new IllegalStateException(String.format(
                        "System \"%s\" requires component types %s which are not registered!",
                        system.getClass().getName(),
                        notRegistered.toString()
                ));
            }

            // FIXME: Calculating this each dispatch is stupid
            val systemComponentMask = system.getRequiredComponents()
                                            .stream()
                                            .map(cluster::getComponentTypeIndexFor)
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
                                            .reduce(new byte[calculateMaskSize(cluster.getNumberOfComponentTypes())],
                                                    SystemDispatcher::setComponentBit,
                                                    SystemDispatcher::combineMaskBytes);

            system.tick(cluster.getEntityStorage().stream()
                               .filter(entity -> entity.compareMask(systemComponentMask)),
                    state,
                    delta
            );
        });
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

    private int calculateMaskSize(int typeIndex) {
        return typeIndex / 8 + ((typeIndex % 8 == 0) ? 0 : 1);
    }
}
