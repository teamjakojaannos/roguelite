package fi.jakojaannos.roguelite.game;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.input.ButtonInput;
import fi.jakojaannos.roguelite.engine.input.InputButton;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.*;
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
        Roguelite roguelite = new Roguelite();
        GameState state = new GameState();
        state.world.registerComponentType(Position.class, Position[]::new);
        state.world.registerComponentType(Velocity.class, Velocity[]::new);
        state.world.registerComponentType(CharacterInput.class, CharacterInput[]::new);
        state.world.registerComponentType(CharacterStats.class, CharacterStats[]::new);
        state.world.registerComponentType(PlayerTag.class, PlayerTag[]::new);
        state.world.registerComponentType(CrosshairTag.class, CrosshairTag[]::new);

        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(new InputEvent(new ButtonInput(InputButton.Keyboard.valueOf(key), ButtonInput.Action.PRESS)));

        roguelite.tick(state, events, 1.0);

        assertEquals(state.inputLeft, left);
        assertEquals(state.inputRight, right);
        assertEquals(state.inputUp, up);
        assertEquals(state.inputDown, down);
    }
}
