package fi.jakojaannos.roguelite.engine.ecs.storage;

import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
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
        assertThrows(AssertionError.class,
                     () -> storage.addComponent(entity == null ? null : new EntityImpl(0, 100),
                                                component == null ? null : new MockComponent()));
    }

    @Test
    void addComponentAddsTheComponent() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        assertTrue(BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), 8));
        assertTrue(storage.getComponent(entity).isPresent());
    }

    @Test
    void removeComponentRemovesTheComponent() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);

        assertFalse(BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), 8));
        assertFalse(storage.getComponent(entity).isPresent());
    }

    @Test
    void tasksAreAppliedInOrderTheyAreCalled_addFirst() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);

        assertFalse(BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), 8));
        assertFalse(storage.getComponent(entity).isPresent());
    }

    @Test
    void tasksAreAppliedInOrderTheyAreCalled_removeFirst() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());

        assertTrue(BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), 8));
        assertTrue(storage.getComponent(entity).isPresent());
    }

    @Test
    void tasksAreAppliedInOrderTheyAreCalled_complex() {
        ComponentStorage<MockComponent> storage = new ComponentStorage<>(100, 8, MockComponent[]::new);
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());

        assertTrue(BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), 8));
        assertTrue(storage.getComponent(entity).isPresent());
    }
}
