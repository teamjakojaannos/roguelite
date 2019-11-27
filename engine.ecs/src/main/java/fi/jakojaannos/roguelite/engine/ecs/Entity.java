package fi.jakojaannos.roguelite.engine.ecs;

public interface Entity {
    /**
     * Gets the unique identifier for this entity. Entity IDs are guaranteed to be unique.
     *
     * @return the unique ID of this entity.
     */
    int getId();

    /**
     * Indicates whether or not this entity is flagged for removal. Entities marked to be removed
     * are destroyed the next time {@link EntityManager#applyModifications()} is called.
     *
     * @return <code>true</code> if this entity is removed or will be removed during the next
     * modification tick. <code>false</code> otherwise.
     */
    boolean isMarkedForRemoval();
}
