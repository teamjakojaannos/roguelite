package fi.jakojaannos.roguelite.engine.ecs.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.entities.EntityImpl;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.NonNull;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ComponentStorage {
    private final int maxComponentTypes;
    private final List<ComponentMap> componentTypes = new ArrayList<>();
    private final Map<Class<? extends Component>, Integer> componentTypeIndices = new HashMap<>();

    private int entityCapacity;

    public ComponentStorage(int entityCapacity, int maxComponentTypes) {
        this.entityCapacity = entityCapacity;
        this.maxComponentTypes = maxComponentTypes;
    }

    public void clear(@NonNull final EntityImpl entity) {
        for (val storage : this.componentTypes) {
            storage.removeComponent(entity);
        }
    }

    public void clear(
            @NonNull final EntityImpl entity,
            @NonNull final Class<? extends Component> except
    ) {
        val componentTypeIndex = getComponentTypeIndexFor(except);
        IntStream.range(0, this.componentTypes.size())
                 .filter(i -> i != componentTypeIndex)
                 .forEach(index -> removeComponentByIndex(entity, index));
    }

    public void clear(
            @NonNull final EntityImpl entity,
            @NonNull final Collection<Class<? extends Component>> except
    ) {
        List<Integer> allowedIndices = except.stream()
                                             .map(this::getComponentTypeIndexFor)
                                             .collect(Collectors.toList());
        IntStream.range(0, this.componentTypes.size())
                 .filter(i -> !allowedIndices.contains(i))
                 .forEach(index -> removeComponentByIndex(entity, index));
    }

    public void resize(final int entityCapacity) {
        if (entityCapacity > this.entityCapacity) {
            this.entityCapacity = entityCapacity;
            for (val storage : this.componentTypes) {
                storage.resize(entityCapacity);
            }
        }
    }

    public <TComponent extends Component> void add(
            @NonNull final EntityImpl entity,
            @NonNull final TComponent component
    ) {
        // noinspection unchecked
        this.componentTypes.get(getComponentTypeIndexFor(component.getClass()))
                           .addComponent(entity, component);
    }

    public void remove(EntityImpl entity, Class<? extends Component> componentClass) {
        removeComponentByIndex(entity, getComponentTypeIndexFor(componentClass));
    }

    public <TComponent extends Component> Integer getComponentTypeIndexFor(
            @NonNull final Class<TComponent> componentClass
    ) {
        return this.componentTypeIndices.computeIfAbsent(componentClass,
                                                         this::createNewComponentStorage);
    }

    private int createNewComponentStorage(Class<? extends Component> componentClass) {
        val index = this.componentTypes.size();
        if (index >= this.maxComponentTypes) {
            throw new IllegalStateException("Too many component types registered!");
        }
        this.componentTypes.add(new ComponentMap<>(
                entityCapacity,
                index,
                componentClass
        ));
        return index;
    }

    private void removeComponentByIndex(
            @NonNull final EntityImpl entity,
            final int componentTypeIndex
    ) {
        this.componentTypes.get(componentTypeIndex)
                           .removeComponent(entity);
    }

    public <TComponent extends Component> Optional<TComponent> get(
            @NonNull final EntityImpl entity,
            @NonNull final Class<? extends TComponent> componentClass
    ) {
        val componentStorage = componentTypes.get(getComponentTypeIndexFor(componentClass));
        // noinspection unchecked
        return (Optional<TComponent>) componentStorage.getComponent(entity);
    }

    public byte[] createComponentBitmask(Collection<Class<? extends Component>> componentTypes) {
        return componentTypes.stream()
                             .map(this::getComponentTypeIndexFor)
                             .reduce(new byte[BitMaskUtils.calculateMaskSize(this.maxComponentTypes)],
                                     BitMaskUtils::setNthBit,
                                     BitMaskUtils::combineMasks);
    }

    public boolean exists(
            @NonNull final EntityImpl entity,
            @NonNull final Class<? extends Component> componentClass
    ) {
        return BitMaskUtils.isNthBitSet(entity.getComponentBitmask(),
                                        getComponentTypeIndexFor(componentClass));
    }
}
