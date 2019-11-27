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
    private BasicWeaponStats weaponStats;

    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder()
                .withSystem("test", new CharacterAttackSystem())
                .build();
        EntityManager entityManager = EntityManager.createNew(256, 32);
        this.world = World.createNew(entityManager);

        Entity player = entityManager.createEntity();
        this.characterInput = new CharacterInput();
        this.characterInput.move.set(0.0);
        this.characterInput.attack = false;
        this.characterStats = new CharacterStats();
        this.characterAbilities = new CharacterAbilities();
        this.weaponStats = new BasicWeaponStats();
        entityManager.addComponentTo(player, new Transform(0.0, 0.0, 0.0));
        entityManager.addComponentTo(player, new Velocity());
        entityManager.addComponentTo(player, this.characterInput);
        entityManager.addComponentTo(player, this.characterAbilities);
        entityManager.addComponentTo(player, this.characterStats);
        entityManager.addComponentTo(player, this.weaponStats);

        entityManager.applyModifications();

        // Warm-up
        for (int i = 0; i < 100; ++i) {
            this.dispatcher.dispatch(this.world, 0.02);
            this.world.getEntityManager().applyModifications();
        }
    }

    @Test
    void characterDoesNotShootWhenInputIsFalse() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        weaponStats.attackRate = 1.0;

        characterInput.attack = false;
        this.dispatcher.dispatch(this.world, 0.02);
        this.world.getEntityManager().applyModifications();

        assertEquals(0, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void characterShootsWhenInputIsTrue() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        weaponStats.attackRate = 1.0;

        characterInput.attack = true;
        this.dispatcher.dispatch(this.world, 0.02);
        this.world.getEntityManager().applyModifications();

        assertEquals(1, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void attackRateLimitsWhenCharacterCanShoot() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        weaponStats.attackRate = 1.0;

        characterInput.attack = true;
        for (int i = 0; i < 175; ++i) {
            this.dispatcher.dispatch(this.world, 0.02);
            this.world.getEntityManager().applyModifications();
        }

        assertEquals(4, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void characterStopsAttackingWhenInputIsSetToFalse() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        weaponStats.attackRate = 1.0;

        characterInput.attack = true;
        this.dispatcher.dispatch(this.world, 0.02);
        this.world.getEntityManager().applyModifications();

        characterInput.attack = false;
        for (int i = 0; i < 200; ++i) {
            this.dispatcher.dispatch(this.world, 0.02);
            this.world.getEntityManager().applyModifications();
        }

        assertEquals(1, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }
}
