package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import org.joml.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraControlSystemTest {
    private SystemDispatcher dispatcher;
    private Camera camera;
    private World world;

    @BeforeEach
    void beforeEach() {
        dispatcher = new DispatcherBuilder()
                .withSystem("test", new CameraControlSystem())
                .build();
        world = World.createNew(Entities.createNew(256, 32));
        Entity cameraEntity = world.getEntities().createEntity();
        camera = new Camera();
        world.getEntities().addComponentTo(cameraEntity, camera);

        world.getEntities().applyModifications();
    }

    @Test
    void doesNothingIfFollowTargetIsNotSet() {
        camera.pos.set(42, 69);
        dispatcher.dispatch(world, 0.02);

        assertEquals(new Vector2d(42, 69), camera.pos);
    }

    @Test
    void isRelativelyCloseToTargetAfterFiveSecondsWhenFollowingFromAfar() {
        Entity target = world.getEntities().createEntity();
        world.getEntities().addComponentTo(target, new Transform(10, 20));
        camera.pos.set(42000, 69000);
        camera.followTarget = target;
        world.getEntities().applyModifications();

        for (int i = 0; i < 5 / 0.02; ++i)
        dispatcher.dispatch(world, 0.02);

        assertTrue(camera.pos.distance(10, 20) < 24.0);
    }
}
