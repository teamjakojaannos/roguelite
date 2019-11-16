package fi.jakojaannos.roguelite.game;

import fi.jakojaannos.roguelite.engine.input.*;
import fi.jakojaannos.roguelite.game.data.GameState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class RogueliteTest {
    @Test
    void inputsAreFalseByDefault() {
        GameState state = Roguelite.createInitialState();
        Roguelite roguelite = new Roguelite();
        roguelite.tick(state, new ArrayDeque<>(), 1.0);

        assertFalse(state.inputLeft);
        assertFalse(state.inputRight);
        assertFalse(state.inputUp);
        assertFalse(state.inputDown);
        assertFalse(state.inputAttack);
    }

    @ParameterizedTest
    @CsvSource({
                       "KEY_A,true,false,false,false",
                       "KEY_D,false,true,false,false",
                       "KEY_W,false,false,true,false",
                       "KEY_S,false,false,false,true",
               })
    void keyInputEventsUpdateStateAccordingly(
            String key,
            boolean left,
            boolean right,
            boolean up,
            boolean down
    ) {
        GameState state = Roguelite.createInitialState();
        Roguelite roguelite = new Roguelite();

        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(new InputEvent(new ButtonInput(InputButton.Keyboard.valueOf(key), ButtonInput.Action.PRESS)));

        roguelite.tick(state, events, 1.0);

        assertEquals(state.inputLeft, left);
        assertEquals(state.inputRight, right);
        assertEquals(state.inputUp, up);
        assertEquals(state.inputDown, down);
    }

    @ParameterizedTest
    @CsvSource({
                       "true,0.0,1.0", "false,0.0,1.0",
                       "true,0.0,0.0", "false,0.0,0.0",
                       "true,0.2,0.3", "false,0.2,0.3",
    })
    void mouseInputEventsUpdateGameState(
            boolean horizontal,
            double initial,
            double newPos
    ) {
        InputAxis.Mouse axisPos = horizontal ? InputAxis.Mouse.X_POS : InputAxis.Mouse.Y_POS;
        GameState state = Roguelite.createInitialState();
        if (horizontal) {
            state.mouseX = initial;
        } else {
            state.mouseY = initial;
        }
        Roguelite roguelite = new Roguelite();

        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(new InputEvent(new AxialInput(axisPos, newPos)));

        roguelite.tick(state, events, 1.0);

        assertEquals(newPos, horizontal ? state.mouseX : state.mouseY);
    }

    @Test
    void mouseButtonEventsUpdateGameState() {
        GameState state = Roguelite.createInitialState();
        Roguelite roguelite = new Roguelite();

        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(0)));

        roguelite.tick(state, events, 1.0);

        assertTrue(state.inputAttack);
    }

    @Test
    void mouseButtonEventsDoNotUpdateGameStateIfButtonIsWrong() {
        GameState state = Roguelite.createInitialState();
        Roguelite roguelite = new Roguelite();

        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(1)));
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(2)));
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(3)));
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(4)));

        roguelite.tick(state, events, 1.0);

        assertFalse(state.inputAttack);
    }

    @Test
    void releasingMouseButtonDisablesInput() {
        GameState state = Roguelite.createInitialState();
        Roguelite roguelite = new Roguelite();
        Queue<InputEvent> events = new ArrayDeque<>();

        // Pressed
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(0)));
        roguelite.tick(state, events, 1.0);

        // Released
        events.offer(ButtonInput.released(InputButton.Mouse.button(0)));
        roguelite.tick(state, events, 1.0);

        assertFalse(state.inputAttack);
    }
}
