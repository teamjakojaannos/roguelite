package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.CollisionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CollisionEventCleanupSystemTest {
    private SystemDispatcher dispatcher;
    private Collider collider;
    private World world;

    @BeforeEach
    void beforeEach() {
        dispatcher = new DispatcherBuilder()
                .withSystem("test", new CollisionEventCleanupSystem())
                .build();
        world = World.createNew(Entities.createNew(256, 32));
        Entity entity = world.getEntities().createEntity();
        collider = new Collider();
        world.getEntities().addComponentTo(entity, collider);

        world.getEntities().applyModifications();
    }

    @Test
    void collisionEventsAreCleanedUp() {
        Entity other = world.getEntities().createEntity();
        collider.collisions.add(new CollisionEvent(other));
        collider.collisions.add(new CollisionEvent(other));
        collider.collisions.add(new CollisionEvent(other));

        dispatcher.dispatch(world, 0.02);

        assertTrue(collider.collisions.isEmpty());
    }
}
