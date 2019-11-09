package fi.jakojäännös.roguelite.engine.ecs;

import lombok.val;

import java.util.*;
import java.util.function.Function;

// TODO: Prevent component type registration after first entity is created

/**
 * A cluster of entities. Contains storage for components in all of the entities in this cluster.
 * Provides accessors for entity components.
 */
public class Cluster {

    private final int entityCapacity;
    private final EntityStorage entityStorage;
    private final List<ComponentStorage> componentTypes = new ArrayList<>();
    private final Map<Class<? extends Component>, Integer> componentTypeIndices = new HashMap<>();
    private final Queue<StorageTask> taskQueue = new ArrayDeque<>();

    public Cluster(int entityCapacity) {
        this.entityStorage = new EntityStorage(entityCapacity);
        this.entityCapacity = entityCapacity;
    }

    public Entity createEntity() {
        val entity = this.entityStorage.create(this.componentTypes.size());
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

    /**
     * Registers a new component type. Initializes component storage for the component type.
     *
     * @param componentClass         Class of the component type to register
     * @param componentArraySupplier Constructor for creating new storage arrays
     * @param <TComponent>           Type of the component
     */
    public <TComponent extends Component> void registerComponentType(
            Class<? extends TComponent> componentClass,
            Function<Integer, TComponent[]> componentArraySupplier
    ) {
        val index = this.componentTypes.size();
        this.componentTypeIndices.put(componentClass, index);

        this.componentTypes.add(new ComponentStorage<>(
                this.entityCapacity,
                index,
                componentArraySupplier
        ));
    }

    /**
     * Adds the component to the entity.
     *
     * @param entity       Entity to add the component to
     * @param component    Component to add
     * @param <TComponent> Type of the component
     */
    public <TComponent extends Component> void addComponentTo(Entity entity, TComponent component) {
        val componentTypeIndex = getComponentTypeIndexFor(component)
                .orElseThrow(() -> new IllegalStateException("Tried adding component of an unregistered type: " + component.getClass()));

        val componentStorage = componentTypes.get(componentTypeIndex);
        // noinspection unchecked
        componentStorage.addComponent(entity, component);
    }

    /**
     * Removes a component of given type from the entity.
     *
     * @param entity    Entity to remove the component from
     * @param component Component to remove
     */
    public void removeComponentFrom(Entity entity, Component component) {
        val componentTypeIndex = getComponentTypeIndexFor(component)
                .orElseThrow(() -> new IllegalStateException("Tried adding component of an unregistered type: " + component.getClass()));

        val componentStorage = componentTypes.get(componentTypeIndex);
        componentStorage.removeComponent(entity);
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
    public <TComponent extends Component> Optional<TComponent> getComponentOf(Entity entity, Class<? extends TComponent> componentClass) {
        val componentTypeIndex = getComponentTypeIndexFor(componentClass)
                .orElseThrow(() -> new IllegalStateException("Tried adding component of an unregistered type: " + componentClass));

        val componentStorage = componentTypes.get(componentTypeIndex);
        // noinspection unchecked
        return (Optional<TComponent>) componentStorage.getComponent(entity);
    }

    private Optional<Integer> getComponentTypeIndexFor(Component component) {
        return Optional.ofNullable(componentTypeIndices.get(component.getClass()));
    }

    private Optional<Integer> getComponentTypeIndexFor(Class<? extends Component> componentClass) {
        return Optional.ofNullable(componentTypeIndices.get(componentClass));
    }

    private interface StorageTask {
        void execute();
    }
}
