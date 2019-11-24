package fi.jakojaannos.roguelite.game.systems;


import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.FollowerEnemyAI;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpawnerSystemTest {

    private SystemDispatcher dispatcher;
    private World world;
    private Entities entities;


    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder()
                .withSystem("test", new SpawnerSystem())
                .build();
        this.entities = Entities.createNew(256, 32);
        this.world = World.createNew(entities);


        entities.applyModifications();
    }


    @ParameterizedTest
    @CsvSource({
            "1.0f,225,0.1f,20",
            "20.0f,5,0.1f,0",
            "3.3f,12,0.3f,1",
            "0.2f,15,0.4f,15",
            "0.0f,15,1.0f,15"
    })
    void spawnerCreatesCorrectAmountOfEnemies(
            double spawnFrequency,
            int nTicks,
            double delta,
            long expectedAmount
    ) {
        Entity spawner = this.entities.createEntity();
        entities.addComponentTo(spawner, new Transform());
        entities.addComponentTo(spawner,
                new SpawnerComponent(spawnFrequency, (entities, spawnerPos, spawnerComponent) -> {
                    Entity e = entities.createEntity();
                    entities.addComponentTo(e, new FollowerEnemyAI(0, 0));
                    return e;
                }));

        entities.applyModifications();

        long enemiesBefore = this.world.getEntities().getEntitiesWith(FollowerEnemyAI.class).count();

        for (int i = 0; i < nTicks; i++) {
            this.dispatcher.dispatch(this.world, delta);
        }

        entities.applyModifications();

        long enemiesAfter = this.world.getEntities().getEntitiesWith(FollowerEnemyAI.class).count();

        assertEquals(expectedAmount, (enemiesAfter - enemiesBefore));
    }


}
