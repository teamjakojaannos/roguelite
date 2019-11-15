package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.EnemyAI;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CharacterAIControllerSystemTest {
    SystemDispatcher<GameState> dispatcher;
    GameState state;
    Entity player, follower;
    Transform playerPos, followerPos;
    CharacterInput followerInput;

    @BeforeEach
    void beforeEach() {
        this.dispatcher = new DispatcherBuilder<GameState>()
                .withSystem("test", new CharacterAIControllerSystem())
                .build();
        this.state = new GameState();
        this.state.world = new Cluster(256);
        this.state.world.registerComponentType(CharacterInput.class, CharacterInput[]::new);
        this.state.world.registerComponentType(Transform.class, Transform[]::new);
        this.state.world.registerComponentType(PlayerTag.class, PlayerTag[]::new);
        this.state.world.registerComponentType(EnemyAI.class, EnemyAI[]::new);

        this.player = this.state.world.createEntity();
        this.playerPos = new Transform();
        this.state.world.addComponentTo(this.player, playerPos);
        this.state.world.addComponentTo(this.player, new PlayerTag());
        this.state.player = player;

        this.follower = this.state.world.createEntity();
        this.followerInput = new CharacterInput();
        this.followerPos = new Transform();
        this.state.world.addComponentTo(this.follower, followerInput);
        this.state.world.addComponentTo(this.follower, followerPos);
        this.state.world.addComponentTo(this.follower, new EnemyAI(100.0f, 0.0f));

        this.state.world.applyModifications();
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

        this.dispatcher.dispatch(this.state.world, this.state, 1.0f);

        if (followerInput.move.length() != 0)
            followerInput.move.normalize();

        assertEquals(expectedDirectionX, followerInput.move.x, 0.0001f);
        assertEquals(expectedDirectionY, followerInput.move.y, 0.0001f);
    }

}
