package fi.jakojaannos.roguelite.engine.ecs.storage;

import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.AccessLevel;
import lombok.Getter;

public class EntityImpl implements fi.jakojaannos.roguelite.engine.ecs.Entity {
    @Getter private final int id;
    @Getter private final byte[] componentBitmask;
    @Getter private boolean markedForRemoval;

    public EntityImpl(int id, int maxComponentTypes) {
        this.id = id;
        this.markedForRemoval = false;

        int nBytes = BitMaskUtils.calculateMaskSize(maxComponentTypes);
        this.componentBitmask = new byte[nBytes];
    }

    void markForRemoval() {
        this.markedForRemoval = true;
    }
}
