package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.DamageInstance;
import fi.jakojaannos.roguelite.game.data.components.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.Health;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthUpdateSystemTest {


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
    void entitiesWithZeroHpAreAddedDeadTag(double maxHp, double currentHp, double damage, boolean shouldBeRemoved) {
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);
        HealthUpdateSystem system = new HealthUpdateSystem();

        Entity entity = entityManager.createEntity();
        Health hp = new Health(maxHp, currentHp);
        entityManager.addComponentTo(entity, hp);
        hp.addDamageInstance(new DamageInstance(damage), 0);

        system.tick(Stream.of(entity), world);

        boolean hasDeadTag = entityManager.getComponentOf(entity, DeadTag.class).isPresent();
        assertEquals(shouldBeRemoved, hasDeadTag);
    }


}
