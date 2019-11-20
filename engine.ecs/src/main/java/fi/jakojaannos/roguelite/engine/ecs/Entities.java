package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.storage.EntitiesImpl;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

public interface Entities {
    static Entities createNew(int entityCapacity, int maxComponentTypes) {
        return new EntitiesImpl(entityCapacity, maxComponentTypes);
    }

    int getMaxComponentTypes();

    @NonNull
    Entity createEntity();

    void destroyEntity(@NonNull Entity entity);

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
    <TComponent extends Component> void removeComponentFrom(
            @NonNull Entity entity,
            @NonNull TComponent component
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

    <TComponent extends Component> Stream<EntityComponentPair> getEntitiesWith(
            @NonNull Class<? extends TComponent> componentType
    );

    @RequiredArgsConstructor
    class EntityComponentPair<TComponent extends Component> {
        @Getter @NonNull private final Entity entity;
        @Getter @NonNull private final TComponent component;
    }
}
