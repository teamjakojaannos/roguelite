package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.world.WorldImpl;

public interface World {
    static World createNew(EntityManager entityManager) {
        return new WorldImpl(entityManager);
    }

    /**
     * Gets the entity/component manager for this world.
     *
     * @return entity/component manager instance for this world
     */
    EntityManager getEntityManager();

    /**
     * Creates or gets the resource of given type.
     *
     * @param resourceType class of the resource to get
     * @param <TResource>  type of the resource to get
     *
     * @return the resource of given type
     */
    <TResource extends Resource> TResource getResource(Class<? extends TResource> resourceType);
}
