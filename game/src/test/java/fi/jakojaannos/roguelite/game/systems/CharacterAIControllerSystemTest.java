package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.FollowerEnemyAI;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CharacterAIControllerSystemTest {
    private SystemDispatcher dispatcher;
    private World world;
    private Entity player, follower;
    private Transform playerPos, followerPos;
    private CharacterInput followerInput;

    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder()
                .withSystem("test", new CharacterAIControllerSystem())
                .build();
        Entities entities = Entities.createNew(256, 32);
        this.world = World.createNew(entities);

        this.player = entities.createEntity();
        this.playerPos = new Transform();
        entities.addComponentTo(this.player, playerPos);
        entities.addComponentTo(this.player, new PlayerTag());
        this.world.getResource(Players.class).player = player;

        this.follower = entities.createEntity();
        this.followerInput = new CharacterInput();
        this.followerPos = new Transform();
        entities.addComponentTo(this.follower, followerInput);
        entities.addComponentTo(this.follower, followerPos);
        entities.addComponentTo(this.follower, new FollowerEnemyAI(100.0f, 0.0f));

        entities.applyModifications();
    }


    @ParameterizedTest
    @CsvSource({
                       "1.0f,1.0f,1.0f,1.0f,0.0f,0.0f",
                       "10.0f,10.0f,10.0f,10.0f,0.0f,0.0f",
                       "1.0f,1.0f,1.0f,0.0f,0f,1f",
                       "1.0f,1.0f,0.0f,1.0f,1f,0f",
                       "1.0f,1.0f,0.0f,0.0f,0.7071f,0.7071f",
                       "50.0f,50.0f,0.0f,0.0f,0.7071f,0.7071f",
                       "50.0f,50.0f,100.0f,100.0f,-0.7071f,-0.7071f",
                       "41.0f,24.0f,7.0f,-32.0f,0.5189f,0.8547f"

               })
    void aiControllerSystemModifiesCharacterInputCorrectly(
            double playerX,
            double playerY,
            double followerX,
            double followerY,
            double expectedDirectionX,
            double expectedDirectionY
    ) {
        this.playerPos.setPosition(playerX, playerY);
        this.followerPos.setPosition(followerX, followerY);

        this.dispatcher.dispatch(this.world, 1.0f);

        if (followerInput.move.length() != 0)
            followerInput.move.normalize();

        assertEquals(expectedDirectionX, followerInput.move.x, 0.0001f);
        assertEquals(expectedDirectionY, followerInput.move.y, 0.0001f);
    }

}
