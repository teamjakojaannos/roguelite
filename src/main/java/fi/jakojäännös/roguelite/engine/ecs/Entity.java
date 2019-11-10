package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.engine.utilities.BitMaskUtils;
import lombok.Getter;

public class Entity {
    @Getter private final int id;
    @Getter private final byte[] componentBitmask;

    @Getter private boolean markedForRemoval;

    Entity(int id, int nComponentTypes) {
        this.id = id;
        this.markedForRemoval = false;

        int nBytes = BitMaskUtils.calculateMaskSize(nComponentTypes);
        this.componentBitmask = new byte[nBytes];
    }

    void markForRemoval() {
        this.markedForRemoval = true;
    }

    boolean hasComponentBit(int componentTypeIndex) {
        if (componentTypeIndex < 0 || componentTypeIndex > this.componentBitmask.length * 8) {
            throw new IllegalArgumentException("Argument out of bounds. [componentTypeIndex: " + componentTypeIndex + "]");
        }

        return BitMaskUtils.isNthBitSet(this.componentBitmask, componentTypeIndex);
    }

    void addComponentBit(int componentTypeIndex) {
        if (componentTypeIndex < 0 || componentTypeIndex > this.componentBitmask.length * 8) {
            throw new IllegalArgumentException("Argument out of bounds. [componentTypeIndex: " + componentTypeIndex + "]");
        }

        BitMaskUtils.setNthBit(this.componentBitmask, componentTypeIndex);
    }

    void removeComponentBit(int componentTypeIndex) {
        if (componentTypeIndex < 0 || componentTypeIndex > this.componentBitmask.length * 8) {
            throw new IllegalArgumentException("Argument out of bounds. [componentTypeIndex: " + componentTypeIndex + "]");
        }

        BitMaskUtils.unsetNthBit(componentBitmask, componentTypeIndex);
    }
}
