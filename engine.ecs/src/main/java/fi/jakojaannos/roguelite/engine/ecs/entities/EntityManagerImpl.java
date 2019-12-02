package fi.jakojaannos.roguelite.engine.ecs.entities;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ComponentGroup;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.components.ComponentStorage;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * A cluster of entities. Contains storage for components in all of the entities in this cluster.
 * Provides accessors for entity components.
 */
@Slf4j
public class EntityManagerImpl implements EntityManager {
    private final int maxComponentTypes;
    private final EntityStorage entityStorage;
    private final ComponentStorage componentStorage;
    private final Queue<StorageTask> taskQueue = new ArrayDeque<>();

    private int entityCapacity;

    public EntityManagerImpl(final int entityCapacity, final int maxComponentTypes) {
        this(entityCapacity, maxComponentTypes, new EntityStorage(entityCapacity), new ComponentStorage(entityCapacity, maxComponentTypes));
    }

    public EntityManagerImpl(
            final int entityCapacity,
            final int maxComponentTypes,
            @NonNull final EntityStorage entityStorage,
            @NonNull final ComponentStorage componentStorage
    ) {
        this.entityStorage = entityStorage;
        this.entityCapacity = entityCapacity;
        this.maxComponentTypes = maxComponentTypes;
        this.componentStorage = componentStorage;
    }

    @Override
    public void registerComponentGroup(@NonNull final ComponentGroup group) {
        this.componentStorage.registerGroup(group);
    }

    @NonNull
    @Override
    public Entity createEntity() {
        val entity = this.entityStorage.create(this.maxComponentTypes);
        if (this.entityStorage.isFull()) {
            resize(this.entityCapacity * 2);
        }

        this.taskQueue.offer(() -> this.entityStorage.spawn(entity));

        return entity;
    }

    @Override
    public void destroyEntity(@NonNull final Entity entityRaw) {
        val entity = (EntityImpl) entityRaw;
        entity.markForRemoval();
        this.taskQueue.offer(() -> {
            this.componentStorage.clear(entity);
            this.entityStorage.remove(entity);
        });
    }

    @Override
    public void applyModifications() {
        while (!this.taskQueue.isEmpty()) {
            this.taskQueue.remove().execute();
        }
    }

    @Override
    public <TComponent extends Component> void addComponentTo(
            @NonNull final Entity entity,
            @NonNull final TComponent component
    ) {
        this.componentStorage.add((EntityImpl) entity, component);
    }

    @Override
    public void removeComponentFrom(
            @NonNull final Entity entity,
            @NonNull final Class<? extends Component> componentClass
    ) {
        this.componentStorage.remove((EntityImpl) entity, componentClass);
    }

    @Override
    public <TComponent extends Component> Optional<TComponent> getComponentOf(
            @NonNull final Entity entity,
            @NonNull final Class<? extends TComponent> componentClass
    ) {
        return this.componentStorage.get((EntityImpl) entity, componentClass);
    }

    @Override
    public boolean hasComponent(
            @NonNull Entity entity,
            @NonNull Class<? extends Component> componentClass
    ) {
        return this.componentStorage.exists((EntityImpl) entity, componentClass);
    }

    @Override
    public boolean hasAnyComponentFromGroup(
            @NonNull final Entity entity,
            @NonNull final ComponentGroup group
    ) {
        return this.componentStorage.anyExists((EntityImpl) entity, group);
    }

    @Override
    public <TComponent extends Component> Stream<EntityComponentPair<TComponent>> getEntitiesWith(
            @NonNull final Class<? extends TComponent> componentClass
    ) {
        return this.entityStorage.stream()
                                 .filter(e -> this.componentStorage.exists(e, componentClass))
                                 .map(e -> new EntityComponentPair<>(e, getComponentOf(e, componentClass).orElseThrow()));
    }

    @Override
    public Stream<Entity> getEntitiesWith(
            @NonNull Collection<Class<? extends Component>> componentTypes
    ) {
        val requiredMask = this.componentStorage.createComponentBitmask(componentTypes);
        return this.entityStorage.stream()
                                 .filter(e -> BitMaskUtils.hasAllBitsOf(e.getComponentBitmask(), requiredMask))
                                 .map(Entity.class::cast);
    }

    @Override
    public @NonNull Stream<Entity> getEntitiesWith(
            @NonNull final Collection<Class<? extends Component>> required,
            @NonNull final Collection<Class<? extends Component>> excluded,
            @NonNull final Collection<ComponentGroup> requiredGroups,
            @NonNull final Collection<ComponentGroup> excludedGroups
    ) {
        val requiredMask = this.componentStorage.createComponentBitmask(required, requiredGroups);
        val excludedMask = this.componentStorage.createComponentBitmask(excluded, excludedGroups);
        return this.entityStorage.stream()
                                 .filter(e -> BitMaskUtils.hasNoneOfTheBitsOf(e.getComponentBitmask(), excludedMask))
                                 .filter(e -> BitMaskUtils.hasAllBitsOf(e.getComponentBitmask(), requiredMask))
                                 .map(Entity.class::cast);
    }

    @Override
    public void clearComponentsExcept(
            @NonNull final Entity entity,
            @NonNull final Class<? extends Component> componentType
    ) {
        this.componentStorage.clear((EntityImpl) entity, componentType);
    }

    @Override
    public void clearComponentsExcept(
            @NonNull final Entity entity,
            @NonNull final Collection<Class<? extends Component>> allowedComponentTypes
    ) {
        this.componentStorage.clear((EntityImpl) entity, allowedComponentTypes);
    }

    private void resize(int entityCapacity) {
        LOG.debug("Resizing EntityManager... Size: {} -> {}", this.entityCapacity, entityCapacity);
        this.entityCapacity = entityCapacity;
        this.entityStorage.resize(entityCapacity);
        this.componentStorage.resize(entityCapacity);
    }

    private interface StorageTask {
        void execute();
    }
}
