package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.test.mock.engine.ecs.MockComponent;
import fi.jakojaannos.roguelite.test.mock.engine.ecs.MockECSSystem;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemDispatcherTest {
    @Test
    void componentsAreAutomaticallyRegisteredDuringDispatch() {
        SystemDispatcher<State> dispatcher = new SystemDispatcher<>(List.of(
                new DispatcherBuilder.SystemEntry<>("system_1", new MockECSSystem<>(List.of(MockComponent.class)), new String[0])
        ));
        Cluster cluster = new Cluster(256, 32);

        assertDoesNotThrow(() -> dispatcher.dispatch(cluster, new State(), 0.0));
        assertDoesNotThrow(() -> cluster.getComponentTypeIndexFor(MockComponent.class));
    }

    @Test
    void dispatchRunsTickOnRegisteredSystems() {
        Cluster cluster = new Cluster(256, 32);

        TestSystem system = new TestSystem();
        SystemDispatcher<State> dispatcher = new SystemDispatcher<>(List.of(
                new DispatcherBuilder.SystemEntry<>("system_1", system, new String[0])
        ));

        dispatcher.dispatch(cluster, new State(), 0.0);
        assertTrue(system.tickCalled);
    }

    private static class State {
    }

    private static class TestSystem implements ECSSystem<State> {
        boolean tickCalled;

        @Override
        public Collection<Class<? extends Component>> getRequiredComponents() {
            return List.of(MockComponent.class);
        }

        @Override
        public void tick(
                Stream<Entity> entities, State state, double delta,
                Cluster cluster
        ) {
            this.tickCalled = true;
        }
    }
}
