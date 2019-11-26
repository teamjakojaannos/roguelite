package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.CharacterStats;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterMovementSystemTest {
    private static final double EPSILON = 0.01;
    private static final double POSITION_EPSILON = 0.2;

    private SystemDispatcher dispatcher;
    private World world;
    private Velocity velocity;
    private Transform transform;
    private CharacterInput characterInput;
    private CharacterStats characterStats;

    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder()
                .withSystem("move", new CharacterMovementSystem())
                .withSystem("velocity", new ApplyVelocitySystem(), "move")
                .build();
        Entities entities = Entities.createNew(256, 32);
        this.world = World.createNew(entities);

        Entity player = entities.createEntity();
        this.characterInput = new CharacterInput();
        this.characterStats = new CharacterStats();
        this.velocity = new Velocity();
        this.transform = new Transform(0.0, 0.0, 0.0);
        entities.addComponentTo(player, this.transform);
        entities.addComponentTo(player, this.velocity);
        entities.addComponentTo(player, this.characterInput);
        entities.addComponentTo(player, this.characterStats);

        this.world.getEntities().applyModifications();
    }

    @ParameterizedTest
    @CsvSource({"1.0f,10.0f", "2.0f,20.0f", "0.1f,1.0f"})
    void characterAcceleratesCorrectly(float acceleration, float expectedSpeedAfter10s) {
        this.characterStats.speed = Float.MAX_VALUE;
        this.characterStats.acceleration = acceleration;
        this.characterInput.move.x = 1.0f;
        for (int i = 0; i < 100; ++i) {
            this.dispatcher.dispatch(this.world, 0.1);
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
            this.dispatcher.dispatch(this.world, 0.1);
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
            this.dispatcher.dispatch(this.world, 0.1);
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
            this.dispatcher.dispatch(this.world, 0.02);
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
            this.dispatcher.dispatch(this.world, 0.02);
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
            this.dispatcher.dispatch(this.world, 0.02);
        }

        assertEquals(expectedX, this.transform.bounds.minX, POSITION_EPSILON);
        assertEquals(expectedY, this.transform.bounds.minY, POSITION_EPSILON);
    }
}
