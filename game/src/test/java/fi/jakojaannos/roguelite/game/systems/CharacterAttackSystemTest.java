package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterAttackSystemTest {
    private SystemDispatcher dispatcher;
    private World world;
    private CharacterInput characterInput;
    private CharacterStats characterStats;
    private CharacterAbilities characterAbilities;

    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder()
                .withSystem("test", new CharacterAttackSystem())
                .build();
        Entities entities = Entities.createNew(256, 32);
        this.world = World.createNew(entities);

        Entity player = entities.createEntity();
        this.characterInput = new CharacterInput();
        this.characterInput.move.set(0.0);
        this.characterInput.attack = false;
        this.characterStats = new CharacterStats();
        this.characterAbilities = new CharacterAbilities();
        entities.addComponentTo(player, new Transform(0.0, 0.0, 0.0));
        entities.addComponentTo(player, new Velocity());
        entities.addComponentTo(player, this.characterInput);
        entities.addComponentTo(player, this.characterAbilities);
        entities.addComponentTo(player, this.characterStats);

        entities.applyModifications();

        // Warm-up
        for (int i = 0; i < 100; ++i) {
            this.dispatcher.dispatch(this.world, 0.02);
            this.world.getEntities().applyModifications();
        }
    }

    @Test
    void characterDoesNotShootWhenInputIsFalse() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        characterStats.attackRate = 1.0;

        characterInput.attack = false;
        this.dispatcher.dispatch(this.world, 0.02);
        this.world.getEntities().applyModifications();

        assertEquals(0, this.world.getEntities().getEntitiesWith(ProjectileTag.class).count());
    }

    @Test
    void characterShootsWhenInputIsTrue() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        characterStats.attackRate = 1.0;

        characterInput.attack = true;
        this.dispatcher.dispatch(this.world, 0.02);
        this.world.getEntities().applyModifications();

        assertEquals(1, this.world.getEntities().getEntitiesWith(ProjectileTag.class).count());
    }

    @Test
    void attackRateLimitsWhenCharacterCanShoot() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        characterStats.attackRate = 1.0;

        characterInput.attack = true;
        for (int i = 0; i < 175; ++i) {
            this.dispatcher.dispatch(this.world, 0.02);
            this.world.getEntities().applyModifications();
        }

        assertEquals(4, this.world.getEntities().getEntitiesWith(ProjectileTag.class).count());
    }

    @Test
    void characterStopsAttackingWhenInputIsSetToFalse() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        characterStats.attackRate = 1.0;

        characterInput.attack = true;
        this.dispatcher.dispatch(this.world, 0.02);
        this.world.getEntities().applyModifications();

        characterInput.attack = false;
        for (int i = 0; i < 200; ++i) {
            this.dispatcher.dispatch(this.world, 0.02);
            this.world.getEntities().applyModifications();
        }

        assertEquals(1, this.world.getEntities().getEntitiesWith(ProjectileTag.class).count());
    }
}
