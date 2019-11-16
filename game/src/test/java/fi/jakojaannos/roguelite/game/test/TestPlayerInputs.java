package fi.jakojaannos.roguelite.game.test;

import fi.jakojaannos.roguelite.engine.input.ButtonInput;
import fi.jakojaannos.roguelite.engine.input.InputButton;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.game.Roguelite;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import org.joml.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestPlayerInputs {
    private GameState state;
    private Roguelite roguelite;

    @BeforeEach
    void beforeEach() {
        state = Roguelite.createInitialState();
        roguelite = new Roguelite();
    }

    @ParameterizedTest
    @ValueSource(strings = {"KEY_W", "KEY_A", "KEY_S", "KEY_D"})
    void pressingWASDCausesThePlayerToMove(String key) {
        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(ButtonInput.pressed(InputButton.Keyboard.valueOf(key)));
        Vector2d initialPosition = state.world.getComponentOf(state.player, Transform.class)
                                              .get()
                                              .getCenter(new Vector2d());

        // Simulate 1s worth of ticks at 0.02 per tick
        for (int i = 0; i < 50; ++i) {
            roguelite.tick(state, events, 0.02);
        }

        Vector2d newPosition = state.world.getComponentOf(state.player, Transform.class)
                                          .get()
                                          .getCenter(new Vector2d());
        assertTrue(initialPosition.sub(newPosition).length() > 0.5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"KEY_W", "KEY_A", "KEY_S", "KEY_D"})
    void playerDoesNotImmediatelyStop(String key) {
        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(ButtonInput.pressed(InputButton.Keyboard.valueOf(key)));

        // Simulate 1s worth of ticks at 0.02 per tick
        for (int i = 0; i < 50; ++i) {
            roguelite.tick(state, events, 0.02);
        }

        Vector2d newPosition = state.world.getComponentOf(state.player, Transform.class)
                                          .get()
                                          .getCenter(new Vector2d());

        events.offer(ButtonInput.released(InputButton.Keyboard.valueOf(key)));
        roguelite.tick(state, events, 0.02);

        Vector2d finalPosition = state.world.getComponentOf(state.player, Transform.class)
                                            .get()
                                            .getCenter(new Vector2d());
        assertTrue(finalPosition.sub(newPosition).length() > 0.05);
    }

    @ParameterizedTest
    @ValueSource(strings = {"KEY_W", "KEY_A", "KEY_S", "KEY_D"})
    void playerStopsWithinTwoSecondsOfReleasingInputs(String key) {
        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(ButtonInput.pressed(InputButton.Keyboard.valueOf(key)));

        // Simulate 1s worth of ticks at 0.02 per tick
        for (int i = 0; i < 50; ++i) {
            roguelite.tick(state, events, 0.02);
        }

        events.offer(ButtonInput.released(InputButton.Keyboard.valueOf(key)));
        // Simulate 2s worth of ticks at 0.02 per tick
        for (int i = 0; i < 100; ++i) {
            roguelite.tick(state, events, 0.02);
        }

        Vector2d newPosition = state.world.getComponentOf(state.player, Transform.class)
                                          .get()
                                          .getCenter(new Vector2d());

        // Simulate 2s worth of ticks at 0.02 per tick
        for (int i = 0; i < 100; ++i) {
            roguelite.tick(state, events, 0.02);
        }

        Vector2d finalPosition = state.world.getComponentOf(state.player, Transform.class)
                                            .get()
                                            .getCenter(new Vector2d());
        assertTrue(finalPosition.sub(newPosition).length() < 0.0001);
    }
}
