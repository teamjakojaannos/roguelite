package fi.jakojaannos.roguelite.engine.ecs.storage;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A cluster of entities. Contains storage for components in all of the entities in this cluster.
 * Provides accessors for entity components.
 */
@Slf4j
public class EntityManagerImpl implements EntityManager {
    private final int maxComponentTypes;
    private final EntityStorage entityStorage;
    private final List<ComponentStorage> componentTypes = new ArrayList<>();
    private final Map<Class<? extends Component>, Integer> componentTypeIndices = new HashMap<>();
    private final Queue<StorageTask> taskQueue = new ArrayDeque<>();

    private int entityCapacity;

    public EntityManagerImpl(int entityCapacity, int maxComponentTypes) {
        this(entityCapacity, maxComponentTypes, ComponentStorage::new);
    }

    public EntityManagerImpl(
            final int entityCapacity,
            final int maxComponentTypes,
            @NonNull final ComponentStorageFactory componentStorageFactory
    ) {
        this.entityStorage = new EntityStorage(entityCapacity);
        this.entityCapacity = entityCapacity;
        this.maxComponentTypes = maxComponentTypes;
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
        while (!this.taskQueue.isEmpty()) {
            this.taskQueue.remove().execute();
        }
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
    public void removeComponentFrom(
            @NonNull Entity entity,
            @NonNull Class<? extends Component> componentClass
    ) {
        removeComponentByIndex(entity, getComponentTypeIndexFor(componentClass));
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
    public boolean hasComponent(
            @NonNull Entity entity,
            @NonNull Class<? extends Component> componentClass
    ) {
        return BitMaskUtils.isNthBitSet(((EntityImpl) entity).getComponentBitmask(),
                                        getComponentTypeIndexFor(componentClass));
    }

    @Override
    public <TComponent extends Component> Stream<EntityComponentPair<TComponent>> getEntitiesWith(
            @NonNull Class<? extends TComponent> componentType
    ) {
        val componentTypeIndex = getComponentTypeIndexFor(componentType);
        return this.entityStorage.stream()
                                 .filter(e -> BitMaskUtils.isNthBitSet(e.getComponentBitmask(), componentTypeIndex))
                                 .map(e -> new EntityComponentPair<>(e, getComponentOf(e, componentType).orElseThrow()));
    }

    @Override
    public Stream<Entity> getEntitiesWith(
            @NonNull Collection<Class<? extends Component>> componentTypes
    ) {
        val requiredMask = componentTypes.stream()
                                         .map(this::getComponentTypeIndexFor)
                                         .reduce(new byte[BitMaskUtils.calculateMaskSize(this.maxComponentTypes)],
                                                 BitMaskUtils::setNthBit,
                                                 BitMaskUtils::combineMasks);
        return this.entityStorage.stream()
                                 .filter(e -> BitMaskUtils.hasAllBitsOf(e.getComponentBitmask(), requiredMask))
                                 .map(Entity.class::cast);
    }

    @Override
    public void clearComponentsExcept(
            @NonNull final Entity entity,
            @NonNull final Class<? extends Component> componentType
    ) {
        val componentTypeIndex = getComponentTypeIndexFor(componentType);
        IntStream.range(0, this.componentTypes.size())
                 .filter(i -> i != componentTypeIndex)
                 .forEach(index -> removeComponentByIndex(entity, index));
    }

    @Override
    public void clearComponentsExcept(
            @NonNull final Entity entity,
            @NonNull final Collection<Class<? extends Component>> allowedComponentTypes
    ) {
        List<Integer> allowedIndices = allowedComponentTypes.stream()
                                                            .map(this::getComponentTypeIndexFor)
                                                            .collect(Collectors.toList());
        IntStream.range(0, this.componentTypes.size())
                 .filter(i -> !allowedIndices.contains(i))
                 .forEach(index -> removeComponentByIndex(entity, index));
    }

    public <TComponent extends Component> Integer getComponentTypeIndexFor(Class<TComponent> componentClass) {
        return this.componentTypeIndices.computeIfAbsent(componentClass,
                                                         this::createNewComponentStorage);
    }

    private int createNewComponentStorage(Class<? extends Component> componentClass) {
        val index = this.componentTypes.size();
        if (index >= this.maxComponentTypes) {
            throw new IllegalStateException("Too many component types registered!");
        }
        this.componentTypes.add(new ComponentStorage<>(
                this.entityCapacity,
                index,
                componentClass
        ));
        return index;
    }

    private void removeComponentByIndex(
            @NonNull final Entity entity,
            final int componentTypeIndex
    ) {
        this.componentTypes.get(componentTypeIndex)
                           .removeComponent((EntityImpl) entity);
    }

    private void resize(int entityCapacity) {
        LOG.debug("Resizing Entity cluster... Size: {} -> {}", this.entityCapacity, entityCapacity);
        this.entityCapacity = entityCapacity;
        this.entityStorage.resize(entityCapacity);
        this.componentTypes.forEach(storage -> storage.resize(entityCapacity));
    }

    private interface StorageTask {
        void execute();
    }

    public interface ComponentStorageFactory {
        <TComponent extends Component> ComponentStorage<TComponent> get(
                int entityCapacity,
                int componentTypeIndex,
                @NonNull Class<TComponent> componentClass
        );
    }
}
