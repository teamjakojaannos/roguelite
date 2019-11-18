package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

// TODO: Prevent component type registration after first entity is created

/**
 * A cluster of entities. Contains storage for components in all of the entities in this cluster.
 * Provides accessors for entity components.
 */
public class Cluster {
    @Getter(AccessLevel.PACKAGE) private final int maxComponentTypes;
    private final EntityStorage entityStorage;
    private final List<ComponentStorage> componentTypes = new ArrayList<>();
    private final Map<Class<? extends Component>, Integer> componentTypeIndices = new HashMap<>();
    private final Queue<StorageTask> taskQueue = new ArrayDeque<>();

    private int entityCapacity;

    public Cluster(int entityCapacity, int maxComponentTypes) {
        this.entityStorage = new EntityStorage(entityCapacity);
        this.entityCapacity = entityCapacity;
        this.maxComponentTypes = maxComponentTypes;
    }

    public Entity createEntity() {
        val entity = this.entityStorage.create(this.maxComponentTypes);
        if (entity.getId() >= this.entityCapacity) {
            resize(this.entityCapacity * 2);
        }

        this.taskQueue.offer(() -> {
            this.entityStorage.spawn(entity);
        });

        return entity;
    }

    public void destroyEntity(Entity entity) {
        entity.markForRemoval();
        this.taskQueue.offer(() -> {
            for (val storage : this.componentTypes) {
                storage.removeComponent(entity);
            }
            this.entityStorage.remove(entity);
        });
    }

    public void applyModifications() {
        for (val storage : this.componentTypes) {
            storage.applyModifications();
        }

        while (!this.taskQueue.isEmpty()) {
            this.taskQueue.remove().execute();
        }
    }

    EntityStorage getEntityStorage() {
        return this.entityStorage;
    }

    /**
     * Adds the component to the entity.
     *
     * @param entity       Entity to add the component to
     * @param component    Component to add
     * @param <TComponent> Type of the component
     */
    public <TComponent extends Component> void addComponentTo(Entity entity, TComponent component) {
        // noinspection unchecked
        this.componentTypes.get(getComponentTypeIndexFor(component.getClass()))
                           .addComponent(entity, component);
    }

    /**
     * Removes a component of given type from the entity.
     *
     * @param entity    Entity to remove the component from
     * @param component Component to remove
     */
    public void removeComponentFrom(Entity entity, Component component) {
        this.componentTypes.get(getComponentTypeIndexFor(component.getClass()))
                           .removeComponent(entity);
    }

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
    public <TComponent extends Component> Optional<TComponent> getComponentOf(
            Entity entity,
            Class<? extends TComponent> componentClass
    ) {
        val componentStorage = componentTypes.get(getComponentTypeIndexFor(componentClass));
        // noinspection unchecked
        return (Optional<TComponent>) componentStorage.getComponent(entity);
    }

    <TComponent extends  Component> Integer getComponentTypeIndexFor(Class<TComponent> componentClass) {
        return this.componentTypeIndices.computeIfAbsent(componentClass,
                                                         clazz -> {
                                                             val index = this.componentTypes.size();

                                                             //noinspection unchecked
                                                             this.componentTypes.add(new ComponentStorage<>(
                                                                     this.entityCapacity,
                                                                     index,
                                                                     size -> (TComponent[]) Array.newInstance(clazz, size)
                                                             ));
                                                             return index;
                                                         });
    }

    private void resize(int entityCapacity) {
        this.entityCapacity = entityCapacity;
        this.entityStorage.resize(entityCapacity);
        this.componentTypes.forEach(storage -> storage.resize(entityCapacity));
    }

    public <TComponent extends Component> Stream<EntityComponentPair> getEntitiesWith(
            @NonNull Class<? extends TComponent> componentType
    ) {
        val componentTypeIndex = getComponentTypeIndexFor(componentType);
        return this.entityStorage.stream()
                                 .filter(e -> BitMaskUtils.isNthBitSet(e.getComponentBitmask(), componentTypeIndex))
                                 .map(e -> new EntityComponentPair<TComponent>(e, getComponentOf(e, componentType).orElseThrow()));
    }

    private interface StorageTask {
        void execute();
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class EntityComponentPair<TComponent extends Component> {
        private final Entity entity;
        private final TComponent component;
    }
}
