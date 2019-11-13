package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.AccessLevel;
import lombok.Getter;

public class Entity {
    @Getter private final int id;
    @Getter(AccessLevel.PACKAGE) private final byte[] componentBitmask;
    @Getter(AccessLevel.PACKAGE) private boolean markedForRemoval;

    private final int nComponentTypes;

    Entity(int id, int nComponentTypes) {
        this.id = id;
        this.nComponentTypes = nComponentTypes;
        this.markedForRemoval = false;

        int nBytes = BitMaskUtils.calculateMaskSize(nComponentTypes);
        this.componentBitmask = new byte[nBytes];
    }

    void markForRemoval() {
        this.markedForRemoval = true;
    }

    boolean hasComponentBit(int componentTypeIndex) {
        ensureValidIndex(componentTypeIndex);

        return BitMaskUtils.isNthBitSet(this.componentBitmask, componentTypeIndex);
    }

    void addComponentBit(int componentTypeIndex) {
        ensureValidIndex(componentTypeIndex);

        BitMaskUtils.setNthBit(this.componentBitmask, componentTypeIndex);
    }

    void removeComponentBit(int componentTypeIndex) {
        ensureValidIndex(componentTypeIndex);

        BitMaskUtils.unsetNthBit(componentBitmask, componentTypeIndex);
    }

    private void ensureValidIndex(int componentTypeIndex) {
        if (componentTypeIndex < 0 || componentTypeIndex >= this.nComponentTypes) {
            throw new IllegalArgumentException("Argument out of bounds. [componentTypeIndex: " + componentTypeIndex + "]");
        }
    }
}
