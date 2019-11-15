package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterMovementSystemTest {
    private static final double EPSILON = 0.01;
    private static final double POSITION_EPSILON = 0.2;

    private SystemDispatcher<GameState> dispatcher;
    private GameState state;
    private Velocity velocity;
    private Transform transform;
    private CharacterInput characterInput;
    private CharacterStats characterStats;

    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder<GameState>()
                .withSystem("test", new CharacterMovementSystem())
                .build();
        this.state = new GameState();
        this.state.world = new Cluster(256);
        this.state.world.registerComponentType(CharacterInput.class, CharacterInput[]::new);
        this.state.world.registerComponentType(CharacterStats.class, CharacterStats[]::new);
        this.state.world.registerComponentType(Velocity.class, Velocity[]::new);
        this.state.world.registerComponentType(Transform.class, Transform[]::new);

        Entity player = this.state.world.createEntity();
        this.characterInput = new CharacterInput();
        this.characterStats = new CharacterStats();
        this.velocity = new Velocity();
        this.transform = new Transform(0.0, 0.0, 0.0);
        this.state.world.addComponentTo(player, this.transform);
        this.state.world.addComponentTo(player, this.velocity);
        this.state.world.addComponentTo(player, this.characterInput);
        this.state.world.addComponentTo(player, this.characterStats);

        this.state.world.applyModifications();
    }

    @ParameterizedTest
    @CsvSource({"1.0f,10.0f", "2.0f,20.0f", "0.1f,1.0f"})
    void characterAcceleratesCorrectly(float acceleration, float expectedSpeedAfter10s) {
        this.characterStats.speed = Float.MAX_VALUE;
        this.characterStats.acceleration = acceleration;
        this.characterInput.move.x = 1.0f;
        for (int i = 0; i < 100; ++i) {
            this.dispatcher.dispatch(this.state.world, this.state, 0.1);
        }

        assertEquals(expectedSpeedAfter10s, this.velocity.velocity.length(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({"-1.0f,1.0f,1.0f,10.0f", "1.0f,-1.0f,2.0f,20.0f", "-1.0f,-1.0f,0.1f,1.0f"})
    void characterAcceleratesCorrectly_diagonalInput(
            float inputH,
            float inputV,
            float acceleration,
            float expectedSpeedAfter10s
    ) {
        this.characterStats.speed = Float.MAX_VALUE;
        this.characterStats.acceleration = acceleration;
        this.characterInput.move.x = inputH;
        this.characterInput.move.y = inputV;
        for (int i = 0; i < 100; ++i) {
            this.dispatcher.dispatch(this.state.world, this.state, 0.1);
        }

        assertEquals(expectedSpeedAfter10s, this.velocity.velocity.length(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({"-1.0f,0.75f,1.0f,10.0f", "1.0f,0.25f,2.0f,20.0f", "0.1f,0.0f,0.1f,1.0f"})
    void characterAcceleratesCorrectly_nonAxisAlignedInput(
            float inputH,
            float inputV,
            float acceleration,
            float expectedSpeedAfter10s
    ) {
        this.characterStats.speed = Float.MAX_VALUE;
        this.characterStats.acceleration = acceleration;
        this.characterInput.move.x = inputH;
        this.characterInput.move.y = inputV;
        for (int i = 0; i < 100; ++i) {
            this.dispatcher.dispatch(this.state.world, this.state, 0.1);
        }

        assertEquals(expectedSpeedAfter10s, this.velocity.velocity.length(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({"1.0f,50.0f", "2.0f,100.0f", "0.1f,5.0f"})
    void characterPositionChangesCorrectly(float acceleration, float expectedPositionAfter10s) {
        this.characterStats.speed = Float.MAX_VALUE;
        this.characterStats.acceleration = acceleration;
        this.characterInput.move.x = 1.0f;
        for (int i = 0; i < 500; ++i) {
            this.dispatcher.dispatch(this.state.world, this.state, 0.02);
        }

        assertEquals(expectedPositionAfter10s, this.transform.bounds.minX, POSITION_EPSILON);
    }

    @ParameterizedTest
    @CsvSource({"-1.0f,1.0f,1.0f,-35.355339f,35.355339f", "1.0f,-1.0f,2.0f,70.710678f,-70.710678f", "-1.0f,-1.0f,0.1f,-3.5355339f,-3.5355339f"})
    void characterPositionChangesCorrectly_diagonalInput(
            float inputH,
            float inputV,
            float acceleration,
            float expectedX,
            float expectedY
    ) {
        this.characterStats.speed = Float.MAX_VALUE;
        this.characterStats.acceleration = acceleration;
        this.characterInput.move.x = inputH;
        this.characterInput.move.y = inputV;
        for (int i = 0; i < 500; ++i) {
            this.dispatcher.dispatch(this.state.world, this.state, 0.02);
        }

        assertEquals(expectedX, this.transform.bounds.minX, POSITION_EPSILON);
        assertEquals(expectedY, this.transform.bounds.minY, POSITION_EPSILON);
    }

    @ParameterizedTest
    @CsvSource({"-1.0,0.75,1.0,-40.0,30.0", "1.0,0.25,2.0,97.014250,24.253563"})
    void characterPositionChangesCorrectly_nonAxisAlignedInput(
            double inputH,
            double inputV,
            double acceleration,
            double expectedX,
            double expectedY
    ) {
        this.characterStats.speed = Float.MAX_VALUE;
        this.characterStats.acceleration = acceleration;
        this.characterInput.move.x = inputH;
        this.characterInput.move.y = inputV;
        for (int i = 0; i < 500; ++i) {
            this.dispatcher.dispatch(this.state.world, this.state, 0.02);
        }

        assertEquals(expectedX, this.transform.bounds.minX, POSITION_EPSILON);
        assertEquals(expectedY, this.transform.bounds.minY, POSITION_EPSILON);
    }
}
