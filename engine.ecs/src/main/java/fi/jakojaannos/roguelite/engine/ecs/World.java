package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.world.WorldImpl;

import java.util.function.Supplier;

public interface World {
    static World createNew(Entities entities) {
        return new WorldImpl(entities);
    }

    /**
     * Gets the entity/component manager for this world.
     *
     * @return entity/component manager instance for this world
     */
    Entities getEntities();

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
