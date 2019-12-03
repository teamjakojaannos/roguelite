package fi.jakojaannos.roguelite.game.data;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import lombok.Getter;
import org.joml.Rectangled;

public class Collision {
    public enum Type {
        ENTITY,
        TILE
    }

    public enum Mode {
        OVERLAP,
        COLLISION
    }

    @Getter  private final Type type;
    @Getter  private final Mode mode;
    @Getter  private final Rectangled bounds;

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

    public Collision(
             final Type type,
             final Mode mode,
             final Rectangled bounds
    ) {
        this.type = type;
        this.mode = mode;
        this.bounds = new Rectangled(bounds);
    }

    public static Collision tile(Mode mode, Rectangled bounds) {
        return new Tile(mode, bounds);
    }

    public static Collision entity(Mode mode, Entity other, Rectangled bounds) {
        return new EntityCollision(mode, other, bounds);
    }

    public static class EntityCollision extends Collision {
         @Getter private final Entity other;

        private EntityCollision(Mode mode, Entity other, Rectangled bounds) {
            super(Type.ENTITY, mode, bounds);
            this.other = other;
        }
    }

    public static class Tile extends Collision {
        private Tile(Mode mode, Rectangled bounds) {
            super(Type.TILE, mode, bounds);
        }
    }
}
