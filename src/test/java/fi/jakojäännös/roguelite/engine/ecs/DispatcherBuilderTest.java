package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.test.mock.engine.ecs.MockECSSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DispatcherBuilderTest {
    @Test
    void buildFailsIfNoClusterIsProvided() {
        Cluster cluster = new Cluster(100);
        assertThrows(IllegalStateException.class, () -> new DispatcherBuilder().build());
    }

    @Test
    void buildReturnsValidSystemDispatcher_noSystems() {
        Cluster cluster = new Cluster(100);
        assertDoesNotThrow(() -> new DispatcherBuilder().withCluster(cluster)
                                                        .build());
    }

    @Test
    void buildReturnsValidSystemDispatcher_mockSystems() {
        Cluster cluster = new Cluster(100);
        assertDoesNotThrow(() -> new DispatcherBuilder().withCluster(cluster)
                                                        .withSystem("mock_1", new MockECSSystem())
                                                        .withSystem("mock_2", new MockECSSystem())
                                                        .withSystem("mock_3", new MockECSSystem(), "mock_2")
                                                        .withSystem("mock_4", new MockECSSystem(), "mock_2")
                                                        .build());
    }
}
