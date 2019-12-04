package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.collision.Collision;
import fi.jakojaannos.roguelite.game.data.collision.CollisionEvent;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CollisionEventCleanupSystemTest {
    private CollisionEventCleanupSystem system;
    private Collider collider;
    private World world;
    private Entity entity;

    @BeforeEach
    void beforeEach() {
        system = new CollisionEventCleanupSystem();
        world = mock(World.class);
        entity = mock(Entity.class);
        collider = new Collider();
        EntityManager entityManager = mock(EntityManager.class);
        when(world.getEntityManager()).thenReturn(entityManager);
        when(entityManager.getComponentOf(eq(entity), eq(Collider.class))).thenReturn(Optional.of(collider));
    }

    @Test
    void collisionEventsAreCleanedUp() {
        Entity other = mock(Entity.class);
        collider.collisions.add(new CollisionEvent(Collision.entity(Collision.Mode.COLLISION, other)));
        collider.collisions.add(new CollisionEvent(Collision.entity(Collision.Mode.COLLISION, other)));
        collider.collisions.add(new CollisionEvent(Collision.entity(Collision.Mode.COLLISION, other)));

        system.tick(Stream.of(entity), world, 0.02);
        assertTrue(collider.collisions.isEmpty());
    }
}
