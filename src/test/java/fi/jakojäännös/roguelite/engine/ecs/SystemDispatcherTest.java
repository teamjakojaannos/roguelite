package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.test.mock.engine.ecs.MockComponent;
import fi.jakojäännös.roguelite.test.mock.engine.ecs.MockECSSystem;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemDispatcherTest {
    @Test
    void constructorThrowsIfClusterIsIncompatible() {
        Cluster cluster = new Cluster(256);
        // MockComponent NOT registered to cluster
        assertThrows(IllegalStateException.class,
                     () -> new SystemDispatcher(cluster, List.of(
                             new DispatcherBuilder.SystemEntry<>("system_1", new MockECSSystem<State>(List.of(MockComponent.class)), new String[0])
                     )));
    }

    @Test
    void dispatchRunsTickOnRegisteredSystems() {
        Cluster cluster = new Cluster(256);
        cluster.registerComponentType(MockComponent.class, MockComponent[]::new);

        TestSystem system = new TestSystem();
        SystemDispatcher<State> dispatcher = new SystemDispatcher<State>(cluster, List.of(
                new DispatcherBuilder.SystemEntry<>("system_1", system, new String[0])
        ));

        dispatcher.dispatch(new State(), 0.0);
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
                Stream<Entity> entities, State state, double delta
        ) {
            this.tickCalled = true;
        }
    }
}
