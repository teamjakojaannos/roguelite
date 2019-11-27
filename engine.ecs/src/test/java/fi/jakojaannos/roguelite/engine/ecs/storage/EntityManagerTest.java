package fi.jakojaannos.roguelite.engine.ecs.storage;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityManagerTest {
    private EntityManager entityManager;

    @BeforeEach
    void beforeEach() {
        entityManager = new EntityManagerImpl(16,
                                              32);
    }

    @Test
    void entitiesCreatedWithCreateEntityHaveValidIDs() {
        Entity entity = entityManager.createEntity();
        assertFalse(entity.isMarkedForRemoval());
        assertTrue(entity.getId() >= 0);
        assertNotEquals(entity.getId(), entityManager.createEntity().getId());
    }

    @Test
    void createEntityCanCreateALargeNumberOfEntitiesWithoutThrowing() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10000; ++i) {
                entityManager.createEntity();
            }
        });
    }

    @Test
    void addingComponentsToNonAppliedEntitiesDoesNotThrow() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);

        assertDoesNotThrow(() -> entityManager.getComponentOf(entity, ComponentA.class).get());
    }

    @Test
    void gettingComponentsOfNonAppliedEntitiesWorks() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);

        assertEquals(component, entityManager.getComponentOf(entity, ComponentA.class).get());
    }

    @Test
    void removingComponentsOfNonAppliedEntitiesWorks() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);
        entityManager.removeComponentFrom(entity, component);

        assertTrue(entityManager.getComponentOf(entity, ComponentA.class).isEmpty());
    }

    @Test
    void getEntitiesWithDoesNotReturnNonAppliedEntities_SingleParameter() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);

        assertTrue(entityManager.getEntitiesWith(ComponentA.class)
                                .noneMatch(c -> c.getComponent().equals(component)));
    }

    @Test
    void getEntitiesWithDoesNotReturnNonAppliedEntities_ListParameter() {
        Entity entity = entityManager.createEntity();
        Component componentA = new ComponentA();
        Component componentB = new ComponentB();
        entityManager.addComponentTo(entity, componentA);
        entityManager.addComponentTo(entity, componentB);

        assertTrue(entityManager.getEntitiesWith(List.of(ComponentA.class, ComponentB.class))
                                .noneMatch(e -> e.getId() == entity.getId()));
    }

    @Test
    void checkingComponentsOfNonAppliedEntitiesWorks() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);

        assertTrue(entityManager.hasComponent(entity, ComponentA.class));
    }

    @Test
    void callingDestroyEntityForNonAppliedEntityMarksItForRemoval() {
        Entity entity = entityManager.createEntity();
        entityManager.destroyEntity(entity);

        assertTrue(entity.isMarkedForRemoval());
    }

    @Test
    void destroyEntityMarksAnEntityForRemoval() {
        Entity entity = entityManager.createEntity();
        entityManager.applyModifications();
        entityManager.destroyEntity(entity);

        assertTrue(entity.isMarkedForRemoval());
    }

    @Test
    void destroyEntityDoesNotImmediatelyRemoveTheEntity() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);
        entityManager.applyModifications();
        entityManager.destroyEntity(entity);

        assertTrue(entity.isMarkedForRemoval());
        assertTrue(entityManager.getComponentOf(entity, ComponentA.class).isPresent());
    }

    @Test
    void entitiesAreDestroyedOnApplyModifications() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);
        entityManager.applyModifications();
        entityManager.destroyEntity(entity);
        entityManager.applyModifications();

        assertTrue(entityManager.getEntitiesWith(ComponentA.class)
                                .noneMatch(e -> e.getEntity().getId() == entity.getId()));
    }

    @Test
    void entitiesSpawnedAndDestroyedOnSameFrameAreDestroyedOnApplyModifications() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);

        entityManager.destroyEntity(entity);
        entityManager.applyModifications();

        assertTrue(entityManager.getEntitiesWith(ComponentA.class)
                                .noneMatch(e -> e.getEntity().getId() == entity.getId()));
    }

    @Test
    void clearComponentsExceptRemovesAllButGivenComponentWhenProvidedWithSingleComponent() {
        Entity entity = entityManager.createEntity();
        Component componentA = new ComponentA();
        Component componentB = new ComponentB();
        Component componentC = new ComponentC();
        entityManager.addComponentTo(entity, componentA);
        entityManager.addComponentTo(entity, componentB);
        entityManager.addComponentTo(entity, componentC);
        entityManager.applyModifications();

        entityManager.clearComponentsExcept(entity, ComponentB.class);

        assertFalse(entityManager.hasComponent(entity, ComponentA.class));
        assertTrue(entityManager.hasComponent(entity, ComponentB.class));
        assertFalse(entityManager.hasComponent(entity, ComponentC.class));
    }

    @Test
    void clearComponentsExceptRemovesAllButGivenComponentsWhenProvidedWithListOfComponents() {
        Entity entity = entityManager.createEntity();
        Component componentA = new ComponentA();
        Component componentB = new ComponentB();
        Component componentC = new ComponentC();
        Component componentD = new ComponentD();
        entityManager.addComponentTo(entity, componentA);
        entityManager.addComponentTo(entity, componentB);
        entityManager.addComponentTo(entity, componentC);
        entityManager.addComponentTo(entity, componentD);
        entityManager.applyModifications();

        entityManager.clearComponentsExcept(entity, List.of(ComponentA.class, ComponentB.class));

        assertTrue(entityManager.hasComponent(entity, ComponentA.class));
        assertTrue(entityManager.hasComponent(entity, ComponentB.class));
        assertFalse(entityManager.hasComponent(entity, ComponentC.class));
        assertFalse(entityManager.hasComponent(entity, ComponentD.class));
    }

    @Test
    void getEntitiesWithReturnsAllExpectedEntities_SingleParameter() {
        Entity entityA = entityManager.createEntity();
        Entity entityB = entityManager.createEntity();
        Entity entityC = entityManager.createEntity();
        Entity entityD = entityManager.createEntity();
        entityManager.addComponentTo(entityA, new ComponentA());
        entityManager.addComponentTo(entityB, new ComponentA());
        entityManager.addComponentTo(entityB, new ComponentB());
        entityManager.addComponentTo(entityC, new ComponentC());
        entityManager.addComponentTo(entityD, new ComponentD());
        entityManager.applyModifications();

        assertTrue(entityManager.getEntitiesWith(ComponentA.class).anyMatch(e -> e.getEntity().getId() == entityA.getId()));
        assertTrue(entityManager.getEntitiesWith(ComponentA.class).anyMatch(e -> e.getEntity().getId() == entityB.getId()));
    }

    @Test
    void getEntitiesWithReturnsAllExpectedEntities_ListParameter() {
        Entity entityA = entityManager.createEntity();
        Entity entityB = entityManager.createEntity();
        Entity entityC = entityManager.createEntity();
        Entity entityD = entityManager.createEntity();
        entityManager.addComponentTo(entityA, new ComponentA());
        entityManager.addComponentTo(entityA, new ComponentB());
        entityManager.addComponentTo(entityB, new ComponentA());
        entityManager.addComponentTo(entityB, new ComponentB());
        entityManager.addComponentTo(entityC, new ComponentC());
        entityManager.addComponentTo(entityD, new ComponentD());
        entityManager.applyModifications();

        assertTrue(entityManager.getEntitiesWith(List.of(ComponentA.class, ComponentB.class)).anyMatch(e -> e.getId() == entityA.getId()));
        assertTrue(entityManager.getEntitiesWith(List.of(ComponentA.class, ComponentB.class)).anyMatch(e -> e.getId() == entityB.getId()));
    }

    @Test
    void addComponentIfAbsentReturnsFalseIfComponentIsPresent() {
        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new ComponentA());

        assertFalse(entityManager.addComponentIfAbsent(entity, new ComponentA()));
    }

    @Test
    void addComponentIfAbsentReturnsTrueIfComponentIsAdded() {
        Entity entity = entityManager.createEntity();

        assertTrue(entityManager.addComponentIfAbsent(entity, new ComponentA()));
    }

    @Test
    void addComponentIfAbsentDoesNotReplaceTheComponent() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);

        entityManager.addComponentIfAbsent(entity, new ComponentA());
        assertEquals(component, entityManager.getComponentOf(entity, ComponentA.class).get());
    }

    @Test
    void removeComponentIfPresentReturnsFalseIfComponentIsNotPresent() {
        Entity entity = entityManager.createEntity();

        assertFalse(entityManager.removeComponentIfPresent(entity, ComponentA.class));
    }

    @Test
    void removeComponentIfPresentReturnsTrueIfComponentIsPresent() {
        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new ComponentA());

        assertTrue(entityManager.removeComponentIfPresent(entity, ComponentA.class));
    }

    @Test
    void removeComponentIfPresentRemovesTheComponentIfComponentIsPresent() {
        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new ComponentA());

        entityManager.removeComponentIfPresent(entity, ComponentA.class);
        assertFalse(entityManager.hasComponent(entity, ComponentA.class));
    }

    @Test
    void registeringSystemsWithTooManyComponentTypesFails() {
        Entity entity = entityManager.createEntity();
        assertThrows(IllegalStateException.class, () -> {
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
            entityManager.addComponentTo(entity, new Component() {
            });
        });
    }

    private static class ComponentA implements Component {
    }

    private static class ComponentB implements Component {
    }

    private static class ComponentC implements Component {
    }

    private static class ComponentD implements Component {
    }
}
