package fi.jakojaannos.roguelite.game.systems.collision;

import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public enum CollisionLayer {
    NONE,
    COLLIDE_ALL,
    OVERLAP_ALL,
    OBSTACLE,
    PLAYER,
    PLAYER_PROJECTILE,
    ENEMY;

    private static final int MASK_SIZE = 1;

    private final byte[] collisionMask = new byte[MASK_SIZE];
    private final byte[] overlapMask = new byte[MASK_SIZE];

    public boolean isSolidTo(final CollisionLayer other) {
        // !hasNone = hasAny
        return other != NONE && BitMaskUtils.isNthBitSet(other.collisionMask, getIndex());
    }

    public boolean canOverlapWith(final CollisionLayer other) {
        return other != NONE && BitMaskUtils.isNthBitSet(other.overlapMask, getIndex());
    }

    private int getIndex() {
        return ordinal() - 1;
    }

    private void setCollidesWith(final CollisionLayer... layers) {
        LOG.trace("Setting collisions for {}", this.name());
        for (val other : layers) {
            LOG.trace("\t-> {}", other.name());
            BitMaskUtils.setNthBit(this.collisionMask, other.getIndex());
        }
    }

    private void setOverlapsWith(final CollisionLayer... layers) {
        LOG.trace("Setting overlaps for {}", this.name());
        for (val other : layers) {
            LOG.trace("\t-> {}", other.name());
            BitMaskUtils.setNthBit(this.overlapMask, other.getIndex());
        }
    }

    public Stream<CollisionLayer> getCollidingLayers() {
        return IntStream.range(0, MASK_SIZE * 8)
                        .filter(i -> BitMaskUtils.isNthBitSet(this.collisionMask, i))
                        .mapToObj(i -> values()[i + 1]);
    }

    public Stream<CollisionLayer> getOverlappingLayers() {
        return IntStream.range(0, MASK_SIZE * 8)
                        .filter(i -> BitMaskUtils.isNthBitSet(this.overlapMask, i))
                        .mapToObj(i -> values()[i + 1]);
    }

    static {
        LOG.debug("Registering collision layers...");

        COLLIDE_ALL.setCollidesWith(values());
        OVERLAP_ALL.setCollidesWith(values());
        PLAYER.setCollidesWith(COLLIDE_ALL, OBSTACLE);
        PLAYER.setOverlapsWith(OVERLAP_ALL, ENEMY);
        PLAYER_PROJECTILE.setCollidesWith(COLLIDE_ALL, OBSTACLE);
        PLAYER_PROJECTILE.setOverlapsWith(OVERLAP_ALL, ENEMY);
        ENEMY.setCollidesWith(COLLIDE_ALL, OBSTACLE);
        ENEMY.setOverlapsWith(OVERLAP_ALL, PLAYER);
    }
}
