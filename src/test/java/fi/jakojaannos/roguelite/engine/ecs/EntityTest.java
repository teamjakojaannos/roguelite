package fi.jakojaannos.roguelite.engine.ecs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {
    @ParameterizedTest
    @CsvSource({"0,0", "1,1", "8,1", "9,2", "100,13"})
    void constructorCreatesMaskWithCorrectSize(int nComponentTypes, int maskSize) {
        assertEquals(maskSize, new Entity(0, nComponentTypes).getComponentBitmask().length);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 69, 1337, Integer.MAX_VALUE})
    void idGetterReturnsIdProvidedInConstructor(int id) {
        assertEquals(id, new Entity(id, 1).getId());
    }

    @Test
    void entitiesAreNotInitiallyMarkedForRemoval() {
        assertFalse(new Entity(0, 1).isMarkedForRemoval());
    }

    @Test
    void isMarkedForRemovalReturnsTrueAfterMarkingForRemoval() {
        Entity entity = new Entity(0, 1);
        entity.markForRemoval();
        assertTrue(entity.isMarkedForRemoval());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, 101, 1000, 100})
    void hasComponentBitThrowsWithInvalidBitIndex(int index) {
        Entity entity = new Entity(0, 100);
        assertThrows(IllegalArgumentException.class, () -> entity.hasComponentBit(index));
    }

    @Test
    void hasComponentBitInitiallyReturnsFalseForAllValidBitsInMask() {
        Entity entity = new Entity(0, 100);
        boolean anyTrue = false;
        for (int i = 0; i < 100; ++i) {
            anyTrue = anyTrue || entity.hasComponentBit(i);
        }

        assertFalse(anyTrue);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 69, 70})
    void hasComponentBitReturnsTrueOnlyForOneComponentWhenOnlyOneIsAdded(int index) {
        Entity entity = new Entity(0, 100);
        entity.addComponentBit(index);
        int countTrue = 0;
        for (int i = 0; i < 100; ++i) {
            countTrue += entity.hasComponentBit(i) ? 1 : 0;
        }

        assertEquals(1, countTrue);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 69, 70})
    void hasComponentBitReturnsTrueForTheAddedComponentWhenOnlyOneIsAdded(int index) {
        Entity entity = new Entity(0, 100);
        entity.addComponentBit(index);
        assertTrue(entity.hasComponentBit(index));
    }

    @Test
    void hasComponentBitReturnsTrueForCorrectNumberOfComponentsWhenManyAreAdded() {
        Entity entity = new Entity(0, 100);
        entity.addComponentBit(0);
        entity.addComponentBit(1);
        entity.addComponentBit(2);
        entity.addComponentBit(69);
        entity.addComponentBit(70);
        int countTrue = 0;
        for (int i = 0; i < 100; ++i) {
            countTrue += entity.hasComponentBit(i) ? 1 : 0;
        }

        assertEquals(5, countTrue);
    }

    @Test
    void hasComponentBitReturnsTrueForAllAddedComponentsWhenManyAreAdded() {
        Entity entity = new Entity(0, 100);
        entity.addComponentBit(0);
        entity.addComponentBit(1);
        entity.addComponentBit(2);
        entity.addComponentBit(69);
        entity.addComponentBit(70);

        assertTrue(entity.hasComponentBit(0));
        assertTrue(entity.hasComponentBit(1));
        assertTrue(entity.hasComponentBit(2));
        assertTrue(entity.hasComponentBit(69));
        assertTrue(entity.hasComponentBit(70));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, 101, 1000, 100})
    void addComponentBitThrowsWithInvalidBitIndex(int index) {
        Entity entity = new Entity(0, 100);
        assertThrows(IllegalArgumentException.class, () -> entity.addComponentBit(index));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 69, 70})
    void removeComponentBitDoesNothingIfRemovingComponentNotAddedWhenThereAreNoComponents(int index) {
        Entity entity = new Entity(0, 100);
        entity.removeComponentBit(index);

        for (int i = 0; i < 100; ++i) {
            assertFalse(entity.hasComponentBit(i));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 69, 70})
    void removeComponentBitDoesNothingIfRemovingComponentNotAddedWhenThereAreMultipleComponents(int index) {
        List<Integer> added = List.of(3, 4, 6, 68, 71);
        Entity entity = new Entity(0, 100);
        added.forEach(entity::addComponentBit);
        entity.removeComponentBit(index);

        for (int i = 0; i < 100; ++i) {
            if (added.contains(i)) {
                assertTrue(entity.hasComponentBit(i));
            } else {
                assertFalse(entity.hasComponentBit(i));
            }
        }
    }
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 69, 70})
    void removeComponentBitRemovesTheComponentIfRemovingValidComponentWhenThereIsExactlyOneComponent(int index) {
        Entity entity = new Entity(0, 100);
        entity.addComponentBit(index);
        entity.removeComponentBit(index);

        for (int i = 0; i < 100; ++i) {
            assertFalse(entity.hasComponentBit(i));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 69, 70})
    void removeComponentBitRemovesTheCorrectComponentIfRemovingValidComponentWhenThereAreMultipleComponents(int index) {
        List<Integer> added = List.of(0, 1, 2, 69, 70);
        Entity entity = new Entity(0, 100);
        added.forEach(entity::addComponentBit);
        entity.removeComponentBit(index);

        for (int i = 0; i < 100; ++i) {
            if (added.contains(i) && i != index) {
                assertTrue(entity.hasComponentBit(i));
            } else {
                assertFalse(entity.hasComponentBit(i));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, 101, 1000, 100})
    void removeComponentBitThrowsWithInvalidBitIndex(int index) {
        Entity entity = new Entity(0, 100);
        assertThrows(IllegalArgumentException.class, () -> entity.removeComponentBit(index));
    }
}
