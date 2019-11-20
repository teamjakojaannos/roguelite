package fi.jakojaannos.roguelite.engine.ecs.storage;

import fi.jakojaannos.roguelite.engine.utilities.IdSupplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class EntityStorage {
    @Getter(AccessLevel.PACKAGE) private int capacity;
    private EntityImpl[] entities;

    private final IdSupplier idSupplier = new IdSupplier();

    public EntityStorage(int capacity) {
        this.capacity = 0;
        this.entities = new EntityImpl[0];
        resize(capacity);
    }

    EntityImpl create(int maxComponentTypes) {
        val entityId = this.idSupplier.get();
        return new EntityImpl(entityId, maxComponentTypes);
    }

    void spawn(@NonNull EntityImpl entity) {
        this.entities[entity.getId()] = entity;
    }

    void remove(@NonNull EntityImpl entity) {
        this.idSupplier.free(entity.getId());
        this.entities[entity.getId()] = null;
    }

    void resize(int capacity) {
        this.capacity = capacity;
        this.entities = Arrays.copyOf(this.entities, this.capacity);
    }

    public Stream<EntityImpl> stream() {
        return Arrays.stream(this.entities).filter(Objects::nonNull);
    }
}
