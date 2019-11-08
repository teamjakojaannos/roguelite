package fi.jakojäännös.roguelite.engine.ecs;

import lombok.Getter;

public class Entity {
    @Getter private final int id;
    private final byte[] componentBitmask;

    @Getter private boolean markedForRemoval;

    public Entity(int id, int nComponentTypes) {
        this.id = id;
        this.markedForRemoval = false;

        int nBytes = divideAndCeil(nComponentTypes, 8);
        this.componentBitmask = new byte[nBytes];
    }

    void markForRemoval() {
        this.markedForRemoval = true;
    }

    boolean hasComponentBit(int componentTypeIndex) {
        if (componentTypeIndex < 0 || componentTypeIndex > this.componentBitmask.length * 8) {
            throw new IllegalArgumentException("Argument out of bounds. [componentTypeIndex: " + componentTypeIndex + "]");
        }

        int m = componentTypeIndex % 8;
        return (this.componentBitmask[divideAndFloor(componentTypeIndex, 8)] & (1 << m)) != 0;
    }

    void addComponentBit(int componentTypeIndex) {
        if (componentTypeIndex < 0 || componentTypeIndex > this.componentBitmask.length * 8) {
            throw new IllegalArgumentException("Argument out of bounds. [componentTypeIndex: " + componentTypeIndex + "]");
        }

        var old = this.componentBitmask[divideAndFloor(componentTypeIndex, 8)];
        int m = componentTypeIndex % 8;
        this.componentBitmask[divideAndFloor(componentTypeIndex, 8)] = (byte) (old | (1 << m));
    }

    void removeComponentBit(int componentTypeIndex) {
        if (componentTypeIndex < 0 || componentTypeIndex > this.componentBitmask.length * 8) {
            throw new IllegalArgumentException("Argument out of bounds. [componentTypeIndex: " + componentTypeIndex + "]");
        }

        var old = this.componentBitmask[divideAndFloor(componentTypeIndex, 8)];
        int m = componentTypeIndex % 8;
        this.componentBitmask[divideAndFloor(componentTypeIndex, 8)] = (byte) (old & ~(1 << m));
    }

    private int divideAndCeil(int a, int b) {
        return a / b + ((a % b == 0) ? 0 : 1);
    }

    private int divideAndFloor(int a, int b) {
        return a / b;
    }
}
