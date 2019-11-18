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
}
