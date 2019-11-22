package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.TileMapLayer;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TileMapCollisionSystemTest {
    private static final TileType DEFAULT = new TileType(69, false);
    private static final TileType WALL = new TileType(1337, true);
    private static final TileType FLOOR = new TileType(42, false);

    private SystemDispatcher dispatcher;
    private Collider collider;
    private TileMapLayer collision;
    private TileMapLayer noCollide;
    private World world;

    @BeforeEach
    void beforeEach() {
        dispatcher = new DispatcherBuilder()
                .withSystem("test", new TileMapCollisionSystem())
                .build();
        world = World.createNew(Entities.createNew(256, 32));
        Entity entity = world.getEntities().createEntity();
        world.getEntities().addComponentTo(entity, new Transform(0.5, 0.5, 1.0));
        world.getEntities().addComponentTo(entity, collider = new Collider());

        Entity collisionLayer = world.getEntities().createEntity();
        world.getEntities().addComponentTo(collisionLayer, collision = new TileMapLayer(new TileMap<>(DEFAULT)));
        collision.collisionEnabled = true;

        Entity noCollideLayer = world.getEntities().createEntity();
        world.getEntities().addComponentTo(noCollideLayer, noCollide = new TileMapLayer(new TileMap<>(DEFAULT)));
        noCollide.collisionEnabled = false;

        world.getEntities().applyModifications();
    }

    @Test
    void whenStandingInNonSolidTileNoCollisionsOccur() {
        collision.getTileMap().setTile(0, 0, FLOOR);
        collision.getTileMap().setTile(1, 0, FLOOR);
        collision.getTileMap().setTile(1, 1, FLOOR);
        collision.getTileMap().setTile(0, 1, FLOOR);

        noCollide.getTileMap().setTile(0, 0, FLOOR);
        noCollide.getTileMap().setTile(1, 0, FLOOR);
        noCollide.getTileMap().setTile(1, 1, FLOOR);
        noCollide.getTileMap().setTile(0, 1, FLOOR);

        dispatcher.dispatch(world, 0.02);

        assertTrue(collider.tileCollisions.isEmpty());
    }

    @Test
    void whenStandingInOneSolidTileOneCollisionOccurs() {
        collision.getTileMap().setTile(0, 0, FLOOR);
        collision.getTileMap().setTile(1, 0, WALL);
        collision.getTileMap().setTile(1, 1, FLOOR);
        collision.getTileMap().setTile(0, 1, FLOOR);

        noCollide.getTileMap().setTile(0, 0, FLOOR);
        noCollide.getTileMap().setTile(1, 0, FLOOR);
        noCollide.getTileMap().setTile(1, 1, FLOOR);
        noCollide.getTileMap().setTile(0, 1, FLOOR);

        dispatcher.dispatch(world, 0.02);

        assertEquals(1, collider.tileCollisions.size());
    }

    @Test
    void whenStandingInFourSolidTilesFourCollisionsOccurs() {
        collision.getTileMap().setTile(0, 0, WALL);
        collision.getTileMap().setTile(1, 0, WALL);
        collision.getTileMap().setTile(1, 1, WALL);
        collision.getTileMap().setTile(0, 1, WALL);

        noCollide.getTileMap().setTile(0, 0, FLOOR);
        noCollide.getTileMap().setTile(1, 0, FLOOR);
        noCollide.getTileMap().setTile(1, 1, FLOOR);
        noCollide.getTileMap().setTile(0, 1, FLOOR);

        dispatcher.dispatch(world, 0.02);

        assertEquals(4, collider.tileCollisions.size());
    }

    @Test
    void whenLayerHasCollisionsDisabledItGeneratesNoEvents() {
        collision.getTileMap().setTile(0, 0, WALL);
        collision.getTileMap().setTile(1, 0, WALL);
        collision.getTileMap().setTile(1, 1, WALL);
        collision.getTileMap().setTile(0, 1, FLOOR);

        noCollide.getTileMap().setTile(0, 0, WALL);
        noCollide.getTileMap().setTile(1, 0, WALL);
        noCollide.getTileMap().setTile(1, 1, WALL);
        noCollide.getTileMap().setTile(0, 1, FLOOR);

        dispatcher.dispatch(world, 0.02);

        assertEquals(3, collider.tileCollisions.size());
    }
}
