package fi.jakojaannos.roguelite.engine.ecs.storage;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entities;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

/**
 * A cluster of entities. Contains storage for components in all of the entities in this cluster.
 * Provides accessors for entity components.
 */
public class EntitiesImpl implements Entities {
    @Getter private final int maxComponentTypes;
    private final EntityStorage entityStorage;
    private final List<ComponentStorage> componentTypes = new ArrayList<>();
    private final Map<Class<? extends Component>, Integer> componentTypeIndices = new HashMap<>();
    private final Queue<StorageTask> taskQueue = new ArrayDeque<>();

    private int entityCapacity;

    public EntitiesImpl(int entityCapacity, int maxComponentTypes) {
        this.entityStorage = new EntityStorage(entityCapacity);
        this.entityCapacity = entityCapacity;
        this.maxComponentTypes = maxComponentTypes;
    }

    @NonNull
    @Override
    public Entity createEntity() {
        val entity = this.entityStorage.create(this.maxComponentTypes);
        if (entity.getId() >= this.entityCapacity) {
            resize(this.entityCapacity * 2);
        }

        this.taskQueue.offer(() -> this.entityStorage.spawn(entity));

        return entity;
    }

    @Override
    public void destroyEntity(@NonNull Entity entityRaw) {
        val entity = (EntityImpl) entityRaw;
        entity.markForRemoval();
        this.taskQueue.offer(() -> {
            for (val storage : this.componentTypes) {
                storage.removeComponent(entity);
            }
            this.entityStorage.remove(entity);
        });
    }

    @Override
    public void applyModifications() {
        for (val storage : this.componentTypes) {
            storage.applyModifications();
        }

        while (!this.taskQueue.isEmpty()) {
            this.taskQueue.remove().execute();
        }
    }

    public EntityStorage getEntityStorage() {
        return this.entityStorage;
    }

    @Override
    public <TComponent extends Component> void addComponentTo(
            @NonNull Entity entity,
            @NonNull TComponent component
    ) {
        // noinspection unchecked
        this.componentTypes.get(getComponentTypeIndexFor(component.getClass()))
                           .addComponent((EntityImpl) entity, component);
    }

    @Override
    public <TComponent extends Component> void removeComponentFrom(
            @NonNull Entity entity,
            @NonNull TComponent component
    ) {
        this.componentTypes.get(getComponentTypeIndexFor(component.getClass()))
                           .removeComponent((EntityImpl) entity);
    }

    @Override
    public <TComponent extends Component> Optional<TComponent> getComponentOf(
            @NonNull Entity entity,
            @NonNull Class<? extends TComponent> componentClass
    ) {
        val componentStorage = componentTypes.get(getComponentTypeIndexFor(componentClass));
        // noinspection unchecked
        return (Optional<TComponent>) componentStorage.getComponent((EntityImpl) entity);
    }

    @Override
    public <TComponent extends Component> Stream<EntityComponentPair> getEntitiesWith(
            @NonNull Class<? extends TComponent> componentType
    ) {
        val componentTypeIndex = getComponentTypeIndexFor(componentType);
        return this.entityStorage.stream()
                                 .filter(e -> BitMaskUtils.isNthBitSet(e.getComponentBitmask(), componentTypeIndex))
                                 .map(e -> new EntityComponentPair<TComponent>(e, getComponentOf(e, componentType).orElseThrow()));
    }

    public <TComponent extends Component> Integer getComponentTypeIndexFor(Class<TComponent> componentClass) {
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

    private interface StorageTask {
        void execute();
    }
}
