package fi.jakojaannos.roguelite.engine.ecs.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.entities.EntityImpl;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
class ComponentMap<TComponent extends Component> {
    /** <code>ComponentTypeIndex</code> for fast checks if some entity has this type of component. */
    private final int componentTypeIndex;

    private int entityCapacity;
    private TComponent[] components;

    ComponentMap(
            final int entityCapacity,
            final int componentTypeIndex,
            @NonNull final Class<TComponent> componentClass
    ) {
        this.componentTypeIndex = componentTypeIndex;

        this.entityCapacity = entityCapacity;
        // noinspection unchecked
        this.components = (TComponent[]) Array.newInstance(componentClass, this.entityCapacity);
    }

    void addComponent(@NonNull EntityImpl entity, @NonNull TComponent component) {
        if (BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), this.componentTypeIndex)) {
            LOG.warn("Add task executed when component bit is already set!");
            return;
        }

        this.components[entity.getId()] = component;
        BitMaskUtils.setNthBit(entity.getComponentBitmask(), this.componentTypeIndex);
    }

    void removeComponent(@NonNull EntityImpl entity) {
        this.components[entity.getId()] = null;
        BitMaskUtils.unsetNthBit(entity.getComponentBitmask(), this.componentTypeIndex);
    }

    Optional<TComponent> getComponent(@NonNull EntityImpl entity) {
        if (!BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), this.componentTypeIndex)) {
            return Optional.empty();
        }

        return Optional.of(this.components[entity.getId()]);
    }

    void resize(int entityCapacity) {
        if (entityCapacity > this.entityCapacity) {
            this.entityCapacity = entityCapacity;
            this.components = Arrays.copyOf(this.components, this.entityCapacity);
        }
    }
}
