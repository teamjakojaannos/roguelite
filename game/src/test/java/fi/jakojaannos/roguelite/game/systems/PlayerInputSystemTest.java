package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.game.Roguelite;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerInputSystemTest {
    SystemDispatcher<GameState> dispatcher;
    GameState state;
    Entity player;
    CharacterInput playerInput;

    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder<GameState>()
                .withSystem("test", new PlayerInputSystem())
                .build();
        this.state = new GameState();
        this.state.world = new Cluster(256);
        this.state.world.registerComponentType(CharacterInput.class, CharacterInput[]::new);
        this.state.world.registerComponentType(PlayerTag.class, PlayerTag[]::new);

        this.player = this.state.world.createEntity();
        this.playerInput = new CharacterInput();
        this.state.world.addComponentTo(this.player, this.playerInput);
        this.state.world.addComponentTo(this.player, new PlayerTag());

        this.state.world.applyModifications();
    }

    @ParameterizedTest
    @CsvSource({
                       "0.0f,0.0f,false,false,false,false",
                       "-1.0f,0.0f,true,false,false,false",
                       "1.0f,0.0f,false,true,false,false",
                       "0.0f,-1.0f,false,false,true,false",
                       "0.0f,1.0f,false,false,false,true",
                       "0.0f,0.0f,false,false,true,true",
                       "0.0f,0.0f,true,true,false,false",
                       "0.0f,0.0f,true,true,true,true",
               })
    void havingInputFlagsSetModifiesCharacterInputCorrectly(
            float expectedHorizontal,
            float expectedVertical,
            boolean left,
            boolean right,
            boolean up,
            boolean down
    ) {
        this.state.inputLeft = left;
        this.state.inputRight = right;
        this.state.inputUp = up;
        this.state.inputDown = down;
        this.dispatcher.dispatch(this.state.world, this.state, 1.0);

        assertEquals(expectedHorizontal, this.playerInput.move.x);
        assertEquals(expectedVertical, this.playerInput.move.y);
    }
}
