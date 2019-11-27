package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.ecs.storage.EntityManagerImpl;
import fi.jakojaannos.roguelite.test.mock.engine.ecs.MockComponent;
import fi.jakojaannos.roguelite.test.mock.engine.ecs.MockECSSystem;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SystemDispatcherTest {
    @Test
    void componentsAreAutomaticallyRegisteredDuringDispatch() {
        SystemDispatcher dispatcher = new SystemDispatcherImpl(List.of(
                new DispatcherBuilder.SystemEntry("system_1", new MockECSSystem(List.of(MockComponent.class)), new String[0])
        ));
        EntityManagerImpl entities = new EntityManagerImpl(256, 32);

        assertDoesNotThrow(() -> dispatcher.dispatch(World.createNew(entities), 0.0));
        assertDoesNotThrow(() -> entities.getComponentTypeIndexFor(MockComponent.class));
    }

    @Test
    void dispatchRunsTickOnRegisteredSystems() {
        EntityManagerImpl entities = new EntityManagerImpl(256, 32);

        TestSystem system = new TestSystem();
        SystemDispatcher dispatcher = new SystemDispatcherImpl(List.of(
                new DispatcherBuilder.SystemEntry("system_1", system, new String[0])
        ));

        dispatcher.dispatch(World.createNew(entities), 0.0);
        assertTrue(system.tickCalled);
    }

    private static class TestSystem implements ECSSystem {
        boolean tickCalled;

        @Override
        public Collection<Class<? extends Component>> getRequiredComponents() {
            return List.of(MockComponent.class);
        }

        @Override
        public void tick(
                Stream<Entity> entities,
                World world,
                double delta
        ) {
            this.tickCalled = true;
        }
    }
}
