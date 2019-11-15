package fi.jakojaannos.roguelite.game;

import fi.jakojaannos.roguelite.engine.input.*;
import fi.jakojaannos.roguelite.game.data.GameState;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RogueliteTest {
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
        GameState state = new GameState(Roguelite.createCluster(256));
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
        GameState state = new GameState(Roguelite.createCluster(256));
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
}
