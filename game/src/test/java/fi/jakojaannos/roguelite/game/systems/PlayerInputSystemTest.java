package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PlayerInputSystemTest {
    private SystemDispatcher<GameState> dispatcher;
    private GameState state;
    private Entity player;
    private CharacterInput input;
    private CharacterAbilities abilities;

    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder<GameState>()
                .withSystem("test", new PlayerInputSystem())
                .build();
        this.state = new GameState();
        this.state.world = new Cluster(256);
        this.state.world.registerComponentType(CharacterAbilities.class, CharacterAbilities[]::new);
        this.state.world.registerComponentType(CharacterInput.class, CharacterInput[]::new);
        this.state.world.registerComponentType(PlayerTag.class, PlayerTag[]::new);

        this.player = this.state.world.createEntity();
        this.input = new CharacterInput();
        this.abilities = new CharacterAbilities();
        this.state.world.addComponentTo(this.player, this.input);
        this.state.world.addComponentTo(this.player, this.abilities);
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
        state.world.applyModifications();

        assertEquals(expectedHorizontal, this.input.move.x);
        assertEquals(expectedVertical, this.input.move.y);
    }

    @ParameterizedTest
    @CsvSource({"0.5,0.5,16,16", "0.25,0.125,8,4", "1.0,0.0,32,0"})
    void attackTargetIsSetToMouseCoordinates(
            double mouseX,
            double mouseY,
            double expectedX,
            double expectedY
    ) {
        this.state.realViewWidth = 32.0f;
        this.state.realViewHeight = 32.0f;
        this.state.mouseX = mouseX;
        this.state.mouseY = mouseY;

        this.dispatcher.dispatch(this.state.world, this.state, 1.0);
        state.world.applyModifications();

        assertEquals(expectedX, abilities.attackTarget.x);
        assertEquals(expectedY, abilities.attackTarget.y);
    }

    @Test
    void havingInputAttackSetUpdatesAttack() {
        this.state.realViewWidth = 32.0f;
        this.state.realViewHeight = 32.0f;
        this.state.inputAttack = false;

        this.dispatcher.dispatch(this.state.world, this.state, 1.0);
        state.world.applyModifications();
        assertFalse(input.attack);

        this.state.inputAttack = true;
        this.dispatcher.dispatch(this.state.world, this.state, 1.0);
        state.world.applyModifications();
        assertTrue(input.attack);

        this.state.inputAttack = false;
        this.dispatcher.dispatch(this.state.world, this.state, 1.0);
        state.world.applyModifications();
        assertFalse(input.attack);
    }
}
