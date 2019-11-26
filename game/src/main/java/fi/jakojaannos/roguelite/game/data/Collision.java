package fi.jakojaannos.roguelite.game.data;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Rectangled;

public class Collision {
    public enum Type {
        ENTITY,
        TILE
    }

    @Getter @NonNull private final Type type;
    @Getter @NonNull private final Rectangled bounds;

    public final boolean isEntity() {
        return this.type == Type.ENTITY;
    }

    public final boolean isTile() {
        return this.type == Type.TILE;
    }

    public EntityCollision getAsEntityCollision() {
        if (this.type != Type.ENTITY) {
            throw new IllegalStateException(String.format("Cannot convert collision of type \"%s\" to ENTITY", this.type));
        }
        return (EntityCollision) this;
    }

    public Tile getAsTileCollision() {
        if (this.type != Type.TILE) {
            throw new IllegalStateException(String.format("Cannot convert collision of type \"%s\" to TILE", this.type));
        }
        return (Tile) this;
    }

    public Collision(@NonNull final Type type, @NonNull final Rectangled bounds) {
        this.type = type;
        this.bounds = new Rectangled(bounds);
    }

    public static Collision tile(Rectangled bounds) {
        return new Tile(bounds);
    }

    public static Collision entity(Entity other, Rectangled bounds) {
        return new EntityCollision(other, bounds);
    }

    public static class EntityCollision extends Collision {
        @NonNull @Getter private final Entity other;

        private EntityCollision(Entity other, Rectangled bounds) {
            super(Type.ENTITY, bounds);
            this.other = other;
        }
    }

    public static class Tile extends Collision {
        private Tile(Rectangled bounds) {
            super(Type.TILE, bounds);
        }
    }
}
