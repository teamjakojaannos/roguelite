package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entities;
import fi.jakojaannos.roguelite.engine.ecs.Entity;

import java.util.function.Function;

public class SpawnerComponent implements Component {

    public final double spawnFrequency;
    public double spawnCoolDown;

    public final Function<Entities, Entity> entityFactory;

    public SpawnerComponent(double spawnFrequency, Function<Entities, Entity> entityFactory) {
        this.spawnFrequency = spawnFrequency;
        this.entityFactory = entityFactory;

        this.spawnCoolDown = 0.0f;
    }


    /**
     * remember to <code>cluster.applyModifications()</code> somewhere
     */
    public static final Function<Entities, Entity> FACTORY_STALKER = entities -> {
        Entity e = entities.createEntity();
        entities.addComponentTo(e, new Transform());
        entities.addComponentTo(e, new Velocity());
        entities.addComponentTo(e, new CharacterInput());
        entities.addComponentTo(e, new CharacterStats());
        entities.addComponentTo(e, new StalkerAI(250.0f, 50.0f, 8.0f));
        return e;
    };


}
