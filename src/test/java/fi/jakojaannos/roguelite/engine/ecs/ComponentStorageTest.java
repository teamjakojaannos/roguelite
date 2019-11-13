package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.test.mock.engine.ecs.MockComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ComponentStorageTest {
    @ParameterizedTest
    @CsvSource({"valid,", ",valid"})
    void addComponentThrowsIfAnyOfTheArgsAreNull(String entity, String component) {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        assertThrows(NullPointerException.class,
                     () -> storage.addComponent(entity == null ? null : new Entity(0, 100),
                                                component == null ? null : new MockComponent()));
    }

    @Test
    void addComponentDoesNotImmediatelyModifyComponentStatus() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        Entity entity = new Entity(0, 100);

        storage.addComponent(entity, new MockComponent());
        assertFalse(entity.hasComponentBit(8));
        assertFalse(storage.getComponent(entity).isPresent());
    }

    @Test
    void addComponentIsAppliedOnApplyModifications() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        Entity entity = new Entity(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.applyModifications();
        assertTrue(entity.hasComponentBit(8));
        assertTrue(storage.getComponent(entity).isPresent());
    }



    @Test
    void removeComponentDoesNotImmediatelyModifyComponentStatus() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        Entity entity = new Entity(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.applyModifications();

        storage.removeComponent(entity);
        assertTrue(entity.hasComponentBit(8));
        assertTrue(storage.getComponent(entity).isPresent());
    }

    @Test
    void removeComponentIsAppliedOnApplyModifications() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        Entity entity = new Entity(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.applyModifications();

        storage.removeComponent(entity);
        storage.applyModifications();

        assertFalse(entity.hasComponentBit(8));
        assertFalse(storage.getComponent(entity).isPresent());
    }

    @Test
    void tasksAreAppliedInOrderTheyAreQueued_addFirst() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        Entity entity = new Entity(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.applyModifications();

        assertFalse(entity.hasComponentBit(8));
        assertFalse(storage.getComponent(entity).isPresent());
    }

    @Test
    void tasksAreAppliedInOrderTheyAreQueued_removeFirst() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        Entity entity = new Entity(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.applyModifications();

        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());
        storage.applyModifications();

        assertTrue(entity.hasComponentBit(8));
        assertTrue(storage.getComponent(entity).isPresent());
    }

    @Test
    void tasksAreAppliedInOrderTheyAreQueued_complex() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        Entity entity = new Entity(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());
        storage.applyModifications();

        assertTrue(entity.hasComponentBit(8));
        assertTrue(storage.getComponent(entity).isPresent());
    }
}
