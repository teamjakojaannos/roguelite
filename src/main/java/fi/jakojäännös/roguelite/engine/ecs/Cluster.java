package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.engine.utilities.IdSupplier;
import lombok.val;

import java.util.*;
import java.util.function.Function;

// TODO: Prevent component type registration after first entity is created

/**
 * A cluster of entities. Contains storage for components in all of the entities in this cluster.
 * Provides accessors for entity components.
 */
public class Cluster {
    private boolean canGrow;
    private int entityCapacity;
    private ArrayList<Entity> entities;
    private final IdSupplier idSupplier = new IdSupplier();

    private final Queue<StorageTask> taskQueue = new ArrayDeque<>();
    private final List<ComponentStorage> componentTypes = new ArrayList<>();
    private final Map<Class<? extends Component>, Integer> componentTypeIndices = new HashMap<>();

    public Cluster() {
        this(256, true);
    }

    public Cluster(int entityCapacity, boolean canGrow) {
        this.entityCapacity = entityCapacity;
        this.canGrow = canGrow;

        this.entities = new ArrayList<>(entityCapacity);
        for (int i = 0; i < entityCapacity; ++i) {
            this.entities.add(null);
        }
    }

    public Entity createEntity() {
        val entityId = this.idSupplier.get();
        val entity = new Entity(entityId, this.componentTypes.size());
        this.taskQueue.offer(() -> {
            if (entityId >= this.entityCapacity) {
                if (!canGrow) {
                    throw new IllegalStateException("Entity capacity overflow");
                }
                // TODO: Some a bit more refined scaling strategy could work better
                this.entityCapacity *= 2;
                this.entities.ensureCapacity(this.entityCapacity);
            }

            this.entities.set(entityId, entity);
        });
        return entity;
    }

    public void destroyEntity(Entity entity) {
        entity.markForRemoval();
        this.taskQueue.offer(() -> {
            for (val storage : this.componentTypes) {
                storage.removeComponent(entity);
            }
            this.idSupplier.free(entity.getId());
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
