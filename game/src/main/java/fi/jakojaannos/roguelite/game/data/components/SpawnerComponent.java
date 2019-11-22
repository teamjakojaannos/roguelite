package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entities;
import fi.jakojaannos.roguelite.engine.ecs.Entity;

import org.joml.Vector2d;

import java.util.Random;

public class SpawnerComponent implements Component {

    public final double spawnFrequency;
    public double spawnCoolDown, maxSpawnDistance;

    public final Random random;
    public final EntityFactory entityFactory;

    private final Vector2d temp = new Vector2d();

    public SpawnerComponent(double spawnFrequency, EntityFactory entityFactory) {
        this(spawnFrequency, entityFactory, 3.0f, System.currentTimeMillis());
    }

    public SpawnerComponent(double spawnFrequency, EntityFactory entityFactory, double maxSpawnDistance, long seed) {
        this.spawnFrequency = spawnFrequency;
        this.maxSpawnDistance = maxSpawnDistance;
        this.entityFactory = entityFactory;

        this.spawnCoolDown = spawnFrequency;
        this.random = new Random(seed);
    }

    public static final EntityFactory FACTORY_DUMMY = (entities, spawnerPos, spawnerComponent) -> {
        Vector2d vec = getRandomSpotAround(spawnerPos, spawnerComponent.maxSpawnDistance, spawnerComponent.random, spawnerComponent.temp);

        Entity e = entities.createEntity();
        entities.addComponentTo(e, new Transform(vec.x, vec.y));
        return e;
    };


    public static final EntityFactory FACTORY_FOLLOWER = (entities, spawnerPos, spawnerComponent) -> {
        Vector2d vec = getRandomSpotAround(spawnerPos, spawnerComponent.maxSpawnDistance, spawnerComponent.random, spawnerComponent.temp);

        Entity e = entities.createEntity();
        entities.addComponentTo(e, new Transform(vec.x, vec.y));
        entities.addComponentTo(e, new Velocity());
        entities.addComponentTo(e, new CharacterInput());
        entities.addComponentTo(e, new CharacterStats(
                4.0,
                100.0,
                800.0, 0, 0
        ));
        entities.addComponentTo(e, new EnemyAI(25.0f, 1.0f));

        return e;
    };


    public static final EntityFactory FACTORY_STALKER = (entities, spawnerPos, spawnerComponent) -> {
        Vector2d vec = getRandomSpotAround(spawnerPos, spawnerComponent.maxSpawnDistance, spawnerComponent.random, spawnerComponent.temp);

        Entity e = entities.createEntity();
        entities.addComponentTo(e, new Transform(vec.x, vec.y));
        entities.addComponentTo(e, new Velocity());
        entities.addComponentTo(e, new CharacterInput());
        entities.addComponentTo(e, new CharacterStats());
        entities.addComponentTo(e, new StalkerAI(250.0f, 50.0f, 8.0f));
        return e;
    };

    /**
     * spawn more spawners. WARNING: will cause lag
     */
    public static final EntityFactory FACTORY_SPAWNER = (entities, spawnerPos, spawnerComponent) -> {
        Vector2d vec = getRandomSpotAround(spawnerPos, spawnerComponent.maxSpawnDistance, spawnerComponent.random, spawnerComponent.temp);

        Entity e = entities.createEntity();
        entities.addComponentTo(e, new Transform(vec.x, vec.y, 0.5f));
        entities.addComponentTo(e,
                new SpawnerComponent(
                        5.0f,
                        SpawnerComponent.FACTORY_SPAWNER,
                        spawnerComponent.maxSpawnDistance,
                        spawnerComponent.random.nextLong()
                ));


        return e;
    };


    private static Vector2d getRandomSpotAround(Transform origin, double maxDist, Random random, Vector2d result) {
        double xDir = random.nextDouble() * 2.0f - 1.0f;
        double yDir = random.nextDouble() * 2.0f - 1.0f;
        result.set(xDir, yDir);
        if (result.lengthSquared() != 0.0) result.normalize();

        result.mul(maxDist * random.nextDouble());
        result.add(origin.getCenterX(), origin.getCenterY());

        return result;
    }


    public interface EntityFactory {
        Entity get(Entities entities, Transform spawnerPos, SpawnerComponent spawnerComponent);
    }

}
