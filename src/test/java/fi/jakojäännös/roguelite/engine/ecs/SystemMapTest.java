package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.test.mock.engine.ecs.MockECSSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class SystemMapTest {
    @Test
    void constructorThrowsIfMapperIsNull() {
        assertThrows(NullPointerException.class, () -> new SystemMap(null, 0));
    }

    @ParameterizedTest
    @CsvSource({"valid,,valid", ",valid,valid"})
    void putThrowsIfAnyOfParametersAreNull(String name, String system, String dependency) {
        SystemMap<State> map = new SystemMap<>(testComponentTypeMapper(), 1);
        map.put("valid", new MockECSSystem<>());

        assertThrows(NullPointerException.class,
                     () -> map.put(
                             name,
                             system == null ? null : new MockECSSystem<>(),
                             dependency
                     ));
    }

    @Test
    void putThrowsIfDependencyIsNotRegistered() {
        SystemMap<State> map = new SystemMap<>(testComponentTypeMapper(), 1);
        assertThrows(IllegalStateException.class,
                     () -> map.put("valid",
                                   new MockECSSystem<>(),
                                   "invalid"));
    }

    @Test
    void putSucceedsIfAllParametersAreValid_noDependencies() {
        SystemMap<State> map = new SystemMap<>(testComponentTypeMapper(), 1);

        assertDoesNotThrow(() -> map.put("valid",
                                         new MockECSSystem<>()));
    }

    @Test
    void putSucceedsIfAllParametersAreValid_singleDependency() {
        SystemMap<State> map = new SystemMap<>(testComponentTypeMapper(), 0);
        map.put("valid_dep_1", new MockECSSystem<>());

        assertDoesNotThrow(() -> map.put("valid",
                                         new MockECSSystem<>(),
                                         "valid_dep_1"));
    }

    @Test
    void putSucceedsIfAllParametersAreValid_manyDependencies() {
        SystemMap<State> map = new SystemMap<>(testComponentTypeMapper(), 0);
        map.put("valid_dep_1", new MockECSSystem<>());
        map.put("valid_dep_2", new MockECSSystem<>());
        map.put("valid_dep_3", new MockECSSystem<>());
        map.put("valid_dep_4", new MockECSSystem<>());

        assertDoesNotThrow(() -> map.put("valid",
                                         new MockECSSystem<>(),
                                         "valid_dep_1",
                                         "valid_dep_2",
                                         "valid_dep_3",
                                         "valid_dep_4"
        ));
    }

    @Test
    void nonPrioritizedStreamGetsAllRegisteredSystems_noDependencies() {
        SystemMap<State> map = new SystemMap<>(testComponentTypeMapper(), 0);
        List<ECSSystem<State>> systems = List.of(
                new MockECSSystem<>(),
                new MockECSSystem<>(),
                new MockECSSystem<>(),
                new MockECSSystem<>()
        );
        for (int i = 0; i < systems.size(); ++i) {
            map.put("valid_" + i, systems.get(i));
        }

        assertTrue(map.nonPrioritizedStream().allMatch(systems::contains));
    }

    @Test
    void nonPrioritizedStreamGetsAllRegisteredSystems_simpleDependencies() {
        SystemMap<State> map = new SystemMap<>(testComponentTypeMapper(), 0);
        List<ECSSystem<State>> systems = List.of(
                new MockECSSystem<>(),
                new MockECSSystem<>(),
                new MockECSSystem<>(),
                new MockECSSystem<>(),
                new MockECSSystem<>()
        );
        map.put("valid_0", systems.get(0));
        for (int i = 1; i < systems.size(); ++i) {
            map.put("valid_" + i, systems.get(i), "valid_" + (i - 1));
        }

        assertTrue(map.nonPrioritizedStream().allMatch(systems::contains));
    }

    @Test
    void forEachPrioritizedThrowsIfConsumerIsNull() {
        SystemMap<State> map = new SystemMap<>(testComponentTypeMapper(), 0);
        assertThrows(NullPointerException.class, () -> map.forEachPrioritized(null));
    }

    @Test
    void forEachPrioritizedSucceedsWhenMapIsEmpty() {
        SystemMap<State> map = new SystemMap<>(testComponentTypeMapper(), 0);
        assertDoesNotThrow(() -> map.forEachPrioritized((system, bytes) -> {
        }));
    }

    @Test
    void forEachPrioritizedIteratesInExpectedOrder_simpleDependencies() {
        SystemMap<State> map = new SystemMap<>(testComponentTypeMapper(), 0);
        List<ECSSystem<State>> systems = new ArrayList<>(List.of(
                new MockECSSystem<>(),
                new MockECSSystem<>(),
                new MockECSSystem<>(),
                new MockECSSystem<>(),
                new MockECSSystem<>())
        );
        map.put("valid_0", systems.get(0));
        for (int i = 1; i < systems.size(); ++i) {
            map.put("valid_" + i, systems.get(i), "valid_" + (i - 1));
        }

        map.forEachPrioritized((system, bytes) -> {
            assertEquals(systems.remove(0), system);
        });
    }

    @Test
    void forEachPrioritizedIteratesInExpectedOrder_complexDependencies() {
        SystemMap<State> map = new SystemMap<>(testComponentTypeMapper(), 0);
        Map<String, ECSSystem<State>> systems = Map.ofEntries(
                Map.entry("valid_0", new MockECSSystem<>()),
                Map.entry("valid_1", new MockECSSystem<>()),
                Map.entry("valid_2", new MockECSSystem<>()),
                Map.entry("valid_3", new MockECSSystem<>()),
                Map.entry("valid_4", new MockECSSystem<>()),
                Map.entry("valid_5", new MockECSSystem<>()),
                Map.entry("valid_6", new MockECSSystem<>()),
                Map.entry("valid_7", new MockECSSystem<>()),
                Map.entry("valid_8", new MockECSSystem<>()),
                Map.entry("valid_9", new MockECSSystem<>()),
                Map.entry("valid_10", new MockECSSystem<>())
        );

        //  0
        // / \
        // 1 2 3 4
        // |   \ /
        // 5    6
        // \    /
        //  7  8
        //  \  /
        //   9
        map.put("valid_10", systems.get("valid_10"));
        map.put("valid_0", systems.get("valid_0"));
        map.put("valid_1", systems.get("valid_1"), "valid_0");
        map.put("valid_2", systems.get("valid_2"), "valid_0");
        map.put("valid_3", systems.get("valid_3"));
        map.put("valid_4", systems.get("valid_4"));
        map.put("valid_5", systems.get("valid_5"), "valid_1");
        map.put("valid_6", systems.get("valid_6"), "valid_3", "valid_4");
        map.put("valid_7", systems.get("valid_7"), "valid_5");
        map.put("valid_8", systems.get("valid_8"), "valid_6");
        map.put("valid_9", systems.get("valid_9"), "valid_7", "valid_8");

        List<ECSSystem<State>> processed = new ArrayList<>();
        map.forEachPrioritized((system, bytes) -> {
            // Ugly hack for finding the name of the system
            String name = systems.entrySet()
                                 .stream()
                                 .filter(e -> e.getValue().equals(system))
                                 .map(Map.Entry::getKey)
                                 .findFirst()
                                 .orElseThrow();

            // Asserts that all dependencies have been met already
            assertTrue(map.getDependencies(name).allMatch(processed::contains));
            processed.add(system);
        });
    }

    private Function<Class<? extends Component>, Optional<Integer>> testComponentTypeMapper() {
        return type -> Optional.of(0);
    }

    private static class State {
    }
}
