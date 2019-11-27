package fi.jakojaannos.roguelite.engine.ecs.storage;

import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import fi.jakojaannos.roguelite.test.mock.engine.ecs.MockComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ComponentStorageTest {
    private ComponentStorage<MockComponent> storage;

    @BeforeEach
    void beforeEach() {
        storage = new ComponentStorage<>(100, 8, MockComponent.class);
    }

    @ParameterizedTest
    @CsvSource({"valid,", ",valid"})
    void addComponentThrowsIfAnyOfTheArgsAreNull(String entity, String component) {
        assertThrows(AssertionError.class,
                     () -> storage.addComponent(entity == null ? null : new EntityImpl(0, 100),
                                                component == null ? null : new MockComponent()));
    }

    @Test
    void addComponentAddsTheComponent() {
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        assertTrue(BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), 8));
        assertTrue(storage.getComponent(entity).isPresent());
    }

    @Test
    void removeComponentRemovesTheComponent() {
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);

        assertFalse(BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), 8));
        assertFalse(storage.getComponent(entity).isPresent());
    }

    @Test
    void tasksAreAppliedInOrderTheyAreCalled_addFirst() {
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);

        assertFalse(BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), 8));
        assertFalse(storage.getComponent(entity).isPresent());
    }

    @Test
    void tasksAreAppliedInOrderTheyAreCalled_removeFirst() {
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());

        assertTrue(BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), 8));
        assertTrue(storage.getComponent(entity).isPresent());
    }

    @Test
    void tasksAreAppliedInOrderTheyAreCalled_complex() {
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
