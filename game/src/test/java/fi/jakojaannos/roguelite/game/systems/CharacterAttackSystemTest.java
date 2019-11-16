package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterAttackSystemTest {
    private SystemDispatcher<GameState> dispatcher;
    private GameState state;
    private Velocity velocity;
    private Transform transform;
    private CharacterInput characterInput;
    private CharacterStats characterStats;
    private CharacterAbilities characterAbilities;

    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder<GameState>()
                .withSystem("test", new CharacterAttackSystem())
                .build();
        this.state = new GameState();
        this.state.world = new Cluster(256);
        this.state.world.registerComponentType(CharacterInput.class, CharacterInput[]::new);
        this.state.world.registerComponentType(CharacterStats.class, CharacterStats[]::new);
        this.state.world.registerComponentType(CharacterAbilities.class, CharacterAbilities[]::new);
        this.state.world.registerComponentType(Velocity.class, Velocity[]::new);
        this.state.world.registerComponentType(Transform.class, Transform[]::new);
        this.state.world.registerComponentType(ProjectileTag.class, ProjectileTag[]::new);

        Entity player = this.state.world.createEntity();
        this.characterInput = new CharacterInput();
        this.characterInput.move.set(0.0);
        this.characterInput.attack = false;
        this.characterStats = new CharacterStats();
        this.characterAbilities = new CharacterAbilities();
        this.velocity = new Velocity();
        this.transform = new Transform(0.0, 0.0, 0.0);
        this.state.world.addComponentTo(player, this.transform);
        this.state.world.addComponentTo(player, this.velocity);
        this.state.world.addComponentTo(player, this.characterInput);
        this.state.world.addComponentTo(player, this.characterAbilities);
        this.state.world.addComponentTo(player, this.characterStats);

        this.state.world.applyModifications();

        // Warm-up
        for (int i = 0; i < 100; ++i) {
            this.dispatcher.dispatch(state.world, state, 0.02);
            this.state.world.applyModifications();
        }
    }

    @Test
    void characterDoesNotShootWhenInputIsFalse() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        characterStats.attackRate = 1.0;

        characterInput.attack = false;
        this.dispatcher.dispatch(state.world, state, 0.02);
        this.state.world.applyModifications();

        assertEquals(0, state.world.getEntitiesWith(ProjectileTag.class).count());
    }

    @Test
    void characterShootsWhenInputIsTrue() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        characterStats.attackRate = 1.0;

        characterInput.attack = true;
        this.dispatcher.dispatch(state.world, state, 0.02);
        this.state.world.applyModifications();

        assertEquals(1, state.world.getEntitiesWith(ProjectileTag.class).count());
    }

    @Test
    void attackRateLimitsWhenCharacterCanShoot() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        characterStats.attackRate = 1.0;

        characterInput.attack = true;
        for (int i = 0; i < 175; ++i) {
            this.dispatcher.dispatch(state.world, state, 0.02);
            this.state.world.applyModifications();
        }

        assertEquals(4, state.world.getEntitiesWith(ProjectileTag.class).count());
    }

    @Test
    void characterStopsAttackingWhenInputIsSetToFalse() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        characterStats.attackRate = 1.0;

        characterInput.attack = true;
        this.dispatcher.dispatch(state.world, state, 0.02);
        this.state.world.applyModifications();

        characterInput.attack = false;
        for (int i = 0; i < 200; ++i) {
            this.dispatcher.dispatch(state.world, state, 0.02);
            this.state.world.applyModifications();
        }

        assertEquals(1, state.world.getEntitiesWith(ProjectileTag.class).count());
    }
}
