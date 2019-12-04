package fi.jakojaannos.roguelite.game.data.collision;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import lombok.Getter;
import org.joml.Rectangled;

public abstract class Collision {
    public enum Type {
        ENTITY,
        TILE
    }

    public enum Mode {
        OVERLAP,
        COLLISION
    }

    @Getter private final Type type;
    @Getter private final Mode mode;

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

    public TileCollision getAsTileCollision() {
        if (this.type != Type.TILE) {
            throw new IllegalStateException(String.format("Cannot convert collision of type \"%s\" to TILE", this.type));
        }
        return (TileCollision) this;
    }

    private Collision(
            final Type type,
            final Mode mode
    ) {
        this.type = type;
        this.mode = mode;
    }

    public static Collision tile(Mode mode) {
        return new TileCollision(mode);
    }

    public static Collision entity(Mode mode, Entity other) {
        return new EntityCollision(mode, other);
    }

    public static class EntityCollision extends Collision {
         @Getter private final Entity other;

        private EntityCollision(Mode mode, Entity other) {
            super(Type.ENTITY, mode);
            this.other = other;
        }
    }

    public static class TileCollision extends Collision {
        private TileCollision(Mode mode) {
            super(Type.TILE, mode);
        }
    }
}
