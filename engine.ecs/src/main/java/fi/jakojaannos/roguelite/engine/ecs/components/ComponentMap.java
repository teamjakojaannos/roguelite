package fi.jakojaannos.roguelite.engine.ecs.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.entities.EntityImpl;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.util.Arrays;

@Slf4j
class ComponentMap<TComponent extends Component> {
    private int entityCapacity;
    private TComponent[] components;

    ComponentMap(
            final int entityCapacity,
            @NonNull final Class<TComponent> componentClass
    ) {
        this.entityCapacity = entityCapacity;
        // noinspection unchecked
        this.components = (TComponent[]) Array.newInstance(componentClass, this.entityCapacity);
    }

    void addComponent(@NonNull EntityImpl entity, @NonNull TComponent component) {
        this.components[entity.getId()] = component;
    }

    void removeComponent(@NonNull EntityImpl entity) {
        this.components[entity.getId()] = null;
    }

    TComponent getComponent(@NonNull EntityImpl entity) {
        return this.components[entity.getId()];
    }

    void resize(int entityCapacity) {
        if (entityCapacity > this.entityCapacity) {
            this.entityCapacity = entityCapacity;
            this.components = Arrays.copyOf(this.components, this.entityCapacity);
        }
    }
}
