package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.AccessLevel;
import lombok.Getter;

public class Entity {
    @Getter private final int id;
    @Getter(AccessLevel.PACKAGE) private final byte[] componentBitmask;
    @Getter(AccessLevel.PACKAGE) private boolean markedForRemoval;

    Entity(int id, int maxComponentTypes) {
        this.id = id;
        this.markedForRemoval = false;

        int nBytes = BitMaskUtils.calculateMaskSize(maxComponentTypes);
        this.componentBitmask = new byte[nBytes];
    }

    void markForRemoval() {
        this.markedForRemoval = true;
    }
}
