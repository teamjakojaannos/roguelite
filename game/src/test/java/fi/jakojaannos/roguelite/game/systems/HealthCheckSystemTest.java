package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Entities;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Health;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthCheckSystemTest {


    @ParameterizedTest
    @CsvSource({
            "1.0f,1.0f,0.0f,false",
            "100.0f,-1.0f,0.0f,true",
            "25.0f,25.0f,25.0f,true",
            "25.0f,25.0f,24.0f,false",
            "25.0f,25.0f,400.0f,true",
            "100.0f,-5.0f,5.0f,true",
            "100.0f,25.0f,25.0f,true",
            "100.0f,25.0f,5.0f,false"
    })
    void entitiesWithZeroHpAreRemoved(double maxHp, double currentHp, double damage, boolean shouldBeRemoved) {
        Entities entities = Entities.createNew(256, 32);
        World world = World.createNew(entities);
        HealthCheckSystem system = new HealthCheckSystem();

        Entity entity = entities.createEntity();
        Health hp = new Health(maxHp, currentHp);
        entities.addComponentTo(entity, hp);
        hp.currentHealth -= damage;

        system.tick(Stream.of(entity), world, 0.2f);

        assertEquals(shouldBeRemoved, entity.isMarkedForRemoval());
    }


}
