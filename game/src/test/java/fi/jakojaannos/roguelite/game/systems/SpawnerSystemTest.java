package fi.jakojaannos.roguelite.game.systems;


import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.FollowerEnemyAI;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SpawnerSystemTest {

    private SpawnerSystem spawnerSystem;
    private SystemDispatcher dispatcher;
    private World world;
    private EntityManager entityManager;


    @BeforeEach
    void beforeEach() {
        this.spawnerSystem = new SpawnerSystem();
        this.dispatcher = new DispatcherBuilder()
                .withSystem("test", spawnerSystem)
                .build();
        this.entityManager = EntityManager.createNew(256, 32);
        this.world = World.createNew(entityManager);


        entityManager.applyModifications();
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
        Entity spawner = this.entityManager.createEntity();
        entityManager.addComponentTo(spawner, new Transform());
        entityManager.addComponentTo(spawner,
                                     new SpawnerComponent(spawnFrequency, (entities, spawnerPos, spawnerComponent) -> {
                    Entity e = entities.createEntity();
                    entities.addComponentTo(e, new FollowerEnemyAI(0, 0));
                    return e;
                }));

        entityManager.applyModifications();

        long enemiesBefore = this.world.getEntityManager().getEntitiesWith(FollowerEnemyAI.class).count();

        for (int i = 0; i < nTicks; i++) {
            this.dispatcher.dispatch(this.world, delta);
        }

        entityManager.applyModifications();

        long enemiesAfter = this.world.getEntityManager().getEntitiesWith(FollowerEnemyAI.class).count();

        assertEquals(expectedAmount, (enemiesAfter - enemiesBefore));
    }

    @Test
    void entityFactoryIsCalledCorrectly() {
        Entity spawnedEnemy = mock(Entity.class);
        SpawnerComponent.EntityFactory mockFactory = mock(SpawnerComponent.EntityFactory.class);
        when(mockFactory.get(any(), any(), any())).thenReturn(spawnedEnemy);

        SpawnerComponent spawnerComponent = new SpawnerComponent(1.0f, mockFactory);
        Entity spawner = entityManager.createEntity();
        entityManager.addComponentTo(spawner, spawnerComponent);
        entityManager.addComponentTo(spawner, new Transform());

        for (int i = 0; i < 20; i++) {
            spawnerSystem.tick(Stream.of(spawner), world, 0.2f);
        }

        verify(mockFactory, times(4)).get(eq(entityManager), any(), eq(spawnerComponent));

    }

}
