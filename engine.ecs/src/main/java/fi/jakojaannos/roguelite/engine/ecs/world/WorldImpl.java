package fi.jakojaannos.roguelite.engine.ecs.world;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.ecs.World;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class WorldImpl implements World {
    private final EntityManager entityManager;
    private final Map<Class<? extends Resource>, Resource> resourceStorage = new HashMap<>();

    public WorldImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    public <TResource extends Resource> TResource getResource(Class<? extends TResource> resourceType) {
        // noinspection unchecked
        return (TResource) this.resourceStorage.computeIfAbsent(resourceType,
                                                                rt -> {
                                                                    try {
                                                                        return resourceType.getConstructor().newInstance();
                                                                    } catch (InstantiationException e) {
                                                                        throw new IllegalStateException(String.format(
                                                                                "Resource type %s represents an abstract class!",
                                                                                rt.getSimpleName()
                                                                        ), e);
                                                                    } catch (IllegalAccessException e) {
                                                                        throw new IllegalStateException(String.format(
                                                                                "Resource type %s default constructor is not accessible!",
                                                                                rt.getSimpleName()
                                                                        ), e);
                                                                    } catch (InvocationTargetException e) {
                                                                        throw new IllegalStateException(String.format(
                                                                                "Error creating resource of type %s, constructor threw an exception",
                                                                                rt.getSimpleName()
                                                                        ), e);
                                                                    } catch (NoSuchMethodException e) {
                                                                        throw new IllegalStateException(String.format(
                                                                                "Resource type %s does not define a default constructor!",
                                                                                rt.getSimpleName()
                                                                        ));
                                                                    }
                                                                });
    }
}
