package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Entities;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.TileMapLayer;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import org.joml.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ApplyVelocitySystemTest {
    private ApplyVelocitySystem system;
    private World world;
    private Entities entities;
    private Entity entity;
    private Velocity velocity;
    private Transform transform;
    private Collider collider;

    @BeforeEach
    void beforeEach() {
        entities = Entities.createNew(256, 32);
        world = World.createNew(entities);
        entity = entities.createEntity();
        entities.addComponentTo(entity, velocity = new Velocity());
        entities.addComponentTo(entity, transform = new Transform(0.0, 0.0, 1));
        entities.addComponentTo(entity, collider = new Collider());

        system = new ApplyVelocitySystem();
    }

    @Test
    void entityWithColliderDoesNotMoveWhenVelocityIsZero() {
        velocity.velocity = new Vector2d(0.0);

        world.getEntities().applyModifications();
        system.tick(Stream.of(entity), world, 1.0);

        assertEquals(0.0, transform.bounds.minX);
        assertEquals(0.0, transform.bounds.minY);
    }

    @Test
    void entityWithColliderMovesWhenVelocityIsNonZero() {
        velocity.velocity = new Vector2d(10.0);

        world.getEntities().applyModifications();
        system.tick(Stream.of(entity), world, 1.0);

        assertEquals(10.0, transform.bounds.minX, 0.02);
        assertEquals(10.0, transform.bounds.minY, 0.02);
    }

    @Test
    void entityWithoutColliderDoesNotMoveWhenVelocityIsZero() {
        Entities entities = Entities.createNew(256, 32);
        World world = World.createNew(entities);
        Entity entity = entities.createEntity();
        entities.addComponentTo(entity, velocity = new Velocity());
        entities.addComponentTo(entity, transform = new Transform(0.0, 0.0, 1));
        velocity.velocity = new Vector2d(0.0);

        world.getEntities().applyModifications();
        system.tick(Stream.of(entity), world, 0.02);

        assertEquals(0.0, transform.bounds.minX);
        assertEquals(0.0, transform.bounds.minY);
    }

    @Test
    void entityWithoutColliderMovesWhenVelocityIsNonZero() {
        Entities entities = Entities.createNew(256, 32);
        World world = World.createNew(entities);
        Entity entity = entities.createEntity();
        entities.addComponentTo(entity, velocity = new Velocity());
        entities.addComponentTo(entity, transform = new Transform(0.0, 0.0, 1));
        velocity.velocity = new Vector2d(10.0);

        world.getEntities().applyModifications();
        system.tick(Stream.of(entity), world, 1.0);

        assertEquals(10.0, transform.bounds.minX);
        assertEquals(10.0, transform.bounds.minY);
    }

    @Test
    void entityWithNonSolidColliderDoesNotBlockMovement() {
        Entity other = entities.createEntity();
        Transform otherTransform = new Transform(1.0, 0.0, 1.0);
        Collider otherCollider = new Collider();
        otherCollider.solid = false;
        entities.addComponentTo(other, otherCollider);
        entities.addComponentTo(other, otherTransform);

        velocity.velocity = new Vector2d(10.0, 0.75);

        world.getEntities().applyModifications();
        for (int i = 0; i < 500; ++i) {
            system.tick(Stream.of(entity), world, 0.002);
        }

        assertEquals(10.0, transform.bounds.minX, 0.01);
        assertEquals(0.75, transform.bounds.minY, 0.01);
    }

    @Test
    void entityWithSolidColliderBlocksMovement() {
        Entity other = entities.createEntity();
        Transform otherTransform = new Transform(1.0, 0.0, 1.0);
        Collider otherCollider = new Collider();
        otherCollider.solid = true;
        entities.addComponentTo(other, otherCollider);
        entities.addComponentTo(other, otherTransform);

        velocity.velocity = new Vector2d(10.0, 0.1);

        world.getEntities().applyModifications();
        for (int i = 0; i < 50; ++i) {
            system.tick(Stream.of(entity), world, 0.02);
        }

        assertEquals(0.0, transform.bounds.minX);
    }

    @Test
    void entitySlidesHorizontallyWhenCollidingAgainstSolidEntityFromBelow() {
        Entity other = entities.createEntity();
        Transform otherTransform = new Transform(0.0, 1.0, 1.0);
        Collider otherCollider = new Collider();
        otherCollider.solid = true;
        entities.addComponentTo(other, otherCollider);
        entities.addComponentTo(other, otherTransform);

        velocity.velocity = new Vector2d(0.1, 1.0);

        world.getEntities().applyModifications();
        for (int i = 0; i < 50; ++i) {
            system.tick(Stream.of(entity), world, 0.02);
        }

        assertNotEquals(0.0, transform.bounds.minX);
        assertEquals(0.0, transform.bounds.minY);
    }

    @Test
    void entitySlidesVerticallyWhenCollidingAgainstSolidEntityFromSide() {
        Entity other = entities.createEntity();
        Transform otherTransform = new Transform(1.0, 0.0, 1.0);
        Collider otherCollider = new Collider();
        otherCollider.solid = true;
        entities.addComponentTo(other, otherCollider);
        entities.addComponentTo(other, otherTransform);

        velocity.velocity = new Vector2d(1.0, 0.1);

        world.getEntities().applyModifications();
        for (int i = 0; i < 50; ++i) {
            system.tick(Stream.of(entity), world, 0.02);
        }

        assertEquals(0.0, transform.bounds.minX);
        assertNotEquals(0.0, transform.bounds.minY);
    }

    @Test
    void tileLayerDoesNotBlockMovementIfCollisionIsDisabledForLayer() {
        Entity other = entities.createEntity();
        TileType empty = new TileType(0, false);
        TileType block = new TileType(1, true);
        TileMap<TileType> tileMap = new TileMap<>(empty);
        tileMap.setTile(1, 0, block);
        TileMapLayer layer = new TileMapLayer(tileMap);
        layer.collisionEnabled = false;
        entities.addComponentTo(other, layer);

        velocity.velocity = new Vector2d(1.0, 0.1);

        world.getEntities().applyModifications();
        for (int i = 0; i < 50; ++i) {
            system.tick(Stream.of(entity), world, 0.02);
        }

        assertEquals(1.0, transform.bounds.minX, 0.01);
        assertEquals(0.1, transform.bounds.minY, 0.01);
    }

    @Test
    void entitySlidesVerticallyWhenCollidingAgainstTileFromSide() {
        Entity other = entities.createEntity();
        TileType empty = new TileType(0, false);
        TileType block = new TileType(1, true);
        TileMap<TileType> tileMap = new TileMap<>(empty);
        tileMap.setTile(1, 0, block);
        entities.addComponentTo(other, new TileMapLayer(tileMap));

        velocity.velocity = new Vector2d(1.0, 0.1);

        world.getEntities().applyModifications();
        for (int i = 0; i < 50; ++i) {
            system.tick(Stream.of(entity), world, 0.02);
        }

        assertEquals(0.0, transform.bounds.minX);
        assertNotEquals(0.0, transform.bounds.minY);
    }

    @Test
    void nonSolidTilesDoNotBlockMovement() {
        Entity other = entities.createEntity();
        TileType empty = new TileType(0, false);
        TileType nonSolid = new TileType(1, false);
        TileMap<TileType> tileMap = new TileMap<>(empty);
        tileMap.setTile(1, 0, nonSolid);
        entities.addComponentTo(other, new TileMapLayer(tileMap));

        velocity.velocity = new Vector2d(1.0, 0.1);

        world.getEntities().applyModifications();
        for (int i = 0; i < 50; ++i) {
            system.tick(Stream.of(entity), world, 0.02);
        }

        assertEquals(1.0, transform.bounds.minX, 0.01);
        assertEquals(0.1, transform.bounds.minY, 0.01);
    }
}
