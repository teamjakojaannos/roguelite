package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.test.mock.engine.ecs.MockComponent;
import fi.jakojaannos.roguelite.test.mock.engine.ecs.MockECSSystem;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemDispatcherTest {
    @Test
    void dispatchThrowsIfClusterIsIncompatible() {
        SystemDispatcher<State> dispatcher = new SystemDispatcher<>(List.of(
                new DispatcherBuilder.SystemEntry<>("system_1", new MockECSSystem<>(List.of(MockComponent.class)), new String[0])
        ));
        Cluster cluster = new Cluster(256);
        // MockComponent NOT registered to cluster
        assertThrows(IllegalStateException.class,
                     () -> dispatcher.dispatch(cluster, new State(), 0.0));
    }

    @Test
    void dispatchRunsTickOnRegisteredSystems() {
        Cluster cluster = new Cluster(256);
        cluster.registerComponentType(MockComponent.class, MockComponent[]::new);

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
