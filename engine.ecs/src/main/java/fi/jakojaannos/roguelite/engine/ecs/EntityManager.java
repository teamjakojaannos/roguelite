package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.entities.EntityManagerImpl;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Allows manipulating entities and their components. Accessor to a {@link World world's} entity
 * storage. All entity-related data mutations happen through the <code>EntityManager</code>.
 */
public interface EntityManager {
    static EntityManager createNew(int entityCapacity, int maxComponentTypes) {
        return new EntityManagerImpl(entityCapacity, maxComponentTypes);
    }

    /**
     * Creates a new entity. The created entity is added to the game world during the next {@link
     * #applyModifications()}
     *
     * @return the entity created
     */
    @NonNull
    Entity createEntity();

    /**
     * Destroys an entity. The entity is marked for removal instantly, and destroyed during the next
     * {@link #applyModifications()}
     *
     * @param entity the entity to mark for removal
     */
    void destroyEntity(@NonNull Entity entity);

    /**
     * Applies all entity mutations. Executes all tasks queued with {@link #createEntity()} and
     * {@link #destroyEntity(Entity)}
     */
    void applyModifications();

    /**
     * Adds the component to the entity.
     *
     * @param entity       Entity to add the component to
     * @param component    Component to add
     * @param <TComponent> Type of the component
     */
    <TComponent extends Component> void addComponentTo(
            @NonNull Entity entity,
            @NonNull TComponent component
    );


    /**
     * Removes a component of given type from the entity.
     *
     * @param entity    Entity to remove the component from
     * @param component Component to remove
     */
    default <TComponent extends Component> void removeComponentFrom(
            @NonNull Entity entity,
            @NonNull TComponent component
    ) {
        removeComponentFrom(entity, component.getClass());
    }

    /**
     * Removes a component of given type from the entity.
     *
     * @param entity         Entity to remove the component from
     * @param componentClass Type of the component to remove
     */
    void removeComponentFrom(
            @NonNull Entity entity,
            @NonNull Class<? extends Component> componentClass
    );

    /**
     * Gets the component of given type from the entity.
     *
     * @param entity         Entity to get components from
     * @param componentClass Component class to get
     * @param <TComponent>   Type of the component
     *
     * @return If component exists, component optional of the component. Otherwise, an empty
     * optional
     */
    <TComponent extends Component> Optional<TComponent> getComponentOf(
            @NonNull Entity entity,
            @NonNull Class<? extends TComponent> componentClass
    );

    /**
     * Checks whether or not the given entity has the specified component.
     *
     * @param entity         entity to check
     * @param componentClass type of the component to look for
     *
     * @return <code>true</code> if the entity has the component, <code>false</code> otherwise
     */
    boolean hasComponent(
            @NonNull Entity entity,
            @NonNull Class<? extends Component> componentClass
    );

    /**
     * Removes all components except the component of given type
     *
     * @param entity        the entity to remove components from
     * @param componentType type of the component not to remove
     */
    void clearComponentsExcept(
            @NonNull Entity entity,
            @NonNull Class<? extends Component> componentType
    );

    /**
     * Removes all components except ones in the given list
     *
     * @param entity         the entity to remove components from
     * @param componentTypes types of the components not to remove
     */
    void clearComponentsExcept(
            @NonNull Entity entity,
            @NonNull Collection<Class<? extends Component>> componentTypes
    );

    /**
     * Gets all entities with given component.
     *
     * @param componentType component type to look for
     * @param <TComponent>  type of the component to look for
     *
     * @return <code>EntityComponentPair</code>s of all the entities and their respective components
     */
    <TComponent extends Component> Stream<EntityComponentPair<TComponent>> getEntitiesWith(
            @NonNull Class<? extends TComponent> componentType
    );

    /**
     * Gets all entities with components of all given types.
     *
     * @param componentTypes types of the components to look for
     *
     * @return Stream of entities with all given component types
     */
    Stream<Entity> getEntitiesWith(
            @NonNull Collection<Class<? extends Component>> componentTypes
    );

    /**
     * Adds the component to the entity if it does not already have a component of the given type.
     * In other words, ensures the entity has a component of given type.
     *
     * @param entity       Entity to add the component to
     * @param component    Component to add
     * @param <TComponent> Type of the component
     *
     * @return <code>true</code> if the component was added, <code>false</code> otherwise
     */
    default <TComponent extends Component> boolean addComponentIfAbsent(
            @NonNull Entity entity,
            @NonNull TComponent component
    ) {
        if (hasComponent(entity, component.getClass())) {
            return false;
        }

        addComponentTo(entity, component);
        return true;
    }

    /**
     * Removes the component from the entity if it has a component of given type. In other words,
     * ensures that the entity has no component of the given type.
     *
     * @param entity         Entity to remove the component from
     * @param componentClass Type of the component to remove
     *
     * @return <code>true</code> if the component was removed, <code>false</code> otherwise
     */
    default boolean removeComponentIfPresent(
            @NonNull Entity entity,
            @NonNull Class<? extends Component> componentClass
    ) {
        if (!hasComponent(entity, componentClass)) {
            return false;
        }

        removeComponentFrom(entity, componentClass);
        return true;
    }

    @RequiredArgsConstructor
    class EntityComponentPair<TComponent extends Component> {
        @Getter @NonNull private final Entity entity;
        @Getter @NonNull private final TComponent component;
    }
}
