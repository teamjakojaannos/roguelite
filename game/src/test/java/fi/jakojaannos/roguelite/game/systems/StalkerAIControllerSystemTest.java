package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StalkerAIControllerSystemTest {

    private SystemDispatcher dispatcher;
    private World world;
    private Transform playerPos, stalkerPos;
    private CharacterStats stalkerStats;
    private StalkerAI stalkerAI;

    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder()
                .withSystem("test", new StalkerAIControllerSystem())
                .build();
        Entities entities = Entities.createNew(256, 32);
        this.world = World.createNew(entities);


        Entity player = entities.createEntity();
        this.playerPos = new Transform();
        entities.addComponentTo(player, playerPos);
        entities.addComponentTo(player, new PlayerTag());
        this.world.getResource(Players.class).player = player;


        Entity stalker = entities.createEntity();
        this.stalkerPos = new Transform();
        entities.addComponentTo(stalker, stalkerPos);
        entities.addComponentTo(stalker, new CharacterInput());
        this.stalkerAI = new StalkerAI(
                100.0f,
                25.0f,
                20.0f);
        entities.addComponentTo(stalker, stalkerAI);
        this.stalkerStats = new CharacterStats(
                1.0,
                100.0,
                800.0
        );
        entities.addComponentTo(stalker, stalkerStats);

        entities.applyModifications();
    }

    @ParameterizedTest
    @CsvSource({
            "0.0f,0.0f,50.0f,50.0f,1.7f",
            "0.0f,0.0f,6.0f,6.0f,0.3f",
            "0.0f,0.0f,1.0f,1.0f,0.3f"
    })
    void stalkerEnemySpeedDependingOnDistanceToPlayerIsSetCorrectly(
            double playerX,
            double playerY,
            double stalkerX,
            double stalkerY,
            double expectedSpeed) {
        this.playerPos.setPosition(playerX, playerY);
        this.stalkerPos.setPosition(stalkerX, stalkerY);
        this.stalkerAI.jumpCoolDown = 100.0f;

        this.dispatcher.dispatch(this.world, 1.0f);

        assertEquals(expectedSpeed, stalkerStats.speed, 0.001f);
    }

    @ParameterizedTest
    @CsvSource({
            "0.0f,0.0f,200.0f,200.0f,false",
            "0.0f,0.0f,8.0f,8.0f,false",
            "0.0f,0.0f,3.0f,3.0f,true",
            "0.0f,0.0f,1.0f,1.0f,true"
    })
    void stalkerLeapAbilityIsUsedWhenNearPlayer(double playerX,
                                                double playerY,
                                                double stalkerX,
                                                double stalkerY,
                                                boolean expectedToUseAbility) {
        this.playerPos.setPosition(playerX, playerY);
        this.stalkerPos.setPosition(stalkerX, stalkerY);
        this.stalkerAI.jumpCoolDown = 0.0f;

        this.dispatcher.dispatch(this.world, 0.2f);

        boolean didUseAbility = (stalkerAI.jumpCoolDown > 0);
        assertEquals(expectedToUseAbility, didUseAbility);
    }


    @Test
    void stalkerLeapAbilityCoolDownWorksCorrectly() {
        this.stalkerAI.jumpCoolDown = 0.0f;
        this.stalkerAI.jumpAbilityGoesCoolDownThisLong = 2.0f;

        this.dispatcher.dispatch(this.world, 0.1f);
        assertEquals(2.0f, this.stalkerAI.jumpCoolDown, 0.001f);

        for (int i = 0; i < 9; i++) {
            this.dispatcher.dispatch(this.world, 0.1f);
        }

        assertEquals(1.1f, stalkerAI.jumpCoolDown, 0.001f);

        // move player out so stalker doesn't instantly leap on them
        this.playerPos.setPosition(100.0f, 100.0f);

        for (int i = 0; i < 11; i++) {
            this.dispatcher.dispatch(this.world, 0.1f);
        }
        assertEquals(0.0f, stalkerAI.jumpCoolDown, 0.001f);
    }


}
