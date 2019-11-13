package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.test.mock.engine.ecs.MockECSSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DispatcherBuilderTest {
    @Test
    void buildReturnsValidSystemDispatcher_noSystems() {
        assertDoesNotThrow(() -> new DispatcherBuilder<>().build());
    }

    @Test
    void buildReturnsValidSystemDispatcher_mockSystems() {
        assertDoesNotThrow(() -> new DispatcherBuilder<>().withSystem("mock_1", new MockECSSystem<>())
                                                          .withSystem("mock_2", new MockECSSystem<>())
                                                          .withSystem("mock_3", new MockECSSystem<>(), "mock_2")
                                                          .withSystem("mock_4", new MockECSSystem<>(), "mock_2")
                                                          .build());
    }
}
