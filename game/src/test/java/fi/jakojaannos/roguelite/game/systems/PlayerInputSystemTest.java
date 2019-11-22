package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PlayerInputSystemTest {
    private SystemDispatcher dispatcher;
    private World world;
    private CharacterInput input;
    private CharacterAbilities abilities;

    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder()
                .withSystem("test", new PlayerInputSystem())
                .build();
        Entities entities = Entities.createNew(256, 32);
        this.world = World.createNew(entities);

        Entity player = entities.createEntity();
        this.input = new CharacterInput();
        this.abilities = new CharacterAbilities();
        entities.addComponentTo(player, this.input);
        entities.addComponentTo(player, this.abilities);
        entities.addComponentTo(player, new PlayerTag());

        entities.applyModifications();
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
        Inputs inputs = this.world.getResource(Inputs.class);
        inputs.inputLeft = left;
        inputs.inputRight = right;
        inputs.inputUp = up;
        inputs.inputDown = down;
        this.dispatcher.dispatch(this.world, 1.0);
        world.getEntities().applyModifications();

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
        Mouse mouse = this.world.getResource(Mouse.class);
        CameraProperties camBounds = this.world.getResource(CameraProperties.class);
        camBounds.viewportWidthInWorldUnits = 32.0f;
        camBounds.viewportHeightInWorldUnits = 32.0f;
        mouse.pos.x = mouseX;
        mouse.pos.y = mouseY;

        this.dispatcher.dispatch(this.world, 1.0);
        this.world.getEntities().applyModifications();

        assertEquals(expectedX, abilities.attackTarget.x);
        assertEquals(expectedY, abilities.attackTarget.y);
    }

    @Test
    void havingInputAttackSetUpdatesAttack() {
        Inputs inputs = this.world.getResource(Inputs.class);
        inputs.inputAttack = false;

        this.dispatcher.dispatch(this.world, 1.0);
        this.world.getEntities().applyModifications();
        assertFalse(input.attack);

        inputs.inputAttack = true;
        this.dispatcher.dispatch(this.world, 1.0);
        this.world.getEntities().applyModifications();
        assertTrue(input.attack);

        inputs.inputAttack = false;
        this.dispatcher.dispatch(this.world, 1.0);
        this.world.getEntities().applyModifications();
        assertFalse(input.attack);
    }
}
