package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.engine.utilities.IdSupplier;
import lombok.NonNull;
import lombok.val;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

class EntityStorage {
    private int capacity;
    private Entity[] entities;

    private final IdSupplier idSupplier = new IdSupplier();

    EntityStorage(int capacity) {
        this.capacity = 0;
        this.entities = new Entity[0];
        resize(capacity);
    }

    Entity create(int nComponentTypes) {
        val entityId = this.idSupplier.get();
        return new Entity(entityId, nComponentTypes);
    }

    void spawn(@NonNull Entity entity) {
        if (entity.getId() >= this.capacity) {
            resize(this.capacity * 2);
        }

        this.entities[entity.getId()] = entity;
    }

    void remove(@NonNull Entity entity) {
        this.idSupplier.free(entity.getId());
        this.entities[entity.getId()] = null;
    }

    private void resize(int capacity) {
        this.capacity = capacity;
        this.entities = Arrays.copyOf(this.entities, this.capacity);
    }

    public Stream<Entity> stream() {
        return Arrays.stream(this.entities).filter(Objects::nonNull);
    }
}
