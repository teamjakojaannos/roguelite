package fi.jakojaannos.roguelite.game.test.stepdefs;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.game.Roguelite;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.archetypes.FollowerArchetype;
import fi.jakojaannos.roguelite.game.data.components.Health;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.joml.Vector2d;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Stepdefs {
    private Roguelite game;
    private GameState state;
    private Queue<InputEvent> inputEvents;

    @Before
    public void before() {
        game = new Roguelite();
        inputEvents = new ArrayDeque<>();
    }

    @Given("the game world just finished loading")
    public void the_game_world_just_finished_loading() {
        state = Roguelite.createInitialState(6969);
    }

    @When("player does nothing")
    public void player_does_nothing() {
        Inputs inputs = state.getWorld().getResource(Inputs.class);
        inputs.inputAttack = false;
        inputs.inputDown = false;
        inputs.inputLeft = false;
        inputs.inputRight = false;
        inputs.inputUp = false;
        inputEvents.clear();
    }

    @When("the game runs for {int} seconds")
    public void the_game_runs_for_x_seconds(int seconds) {
        int ticks = (int) (seconds / 0.02);
        for (int i = 0; i < ticks; ++i) {
            game.tick(state, inputEvents, 0.02);
            game.getTime().progressGameTime(20L);
        }
    }

    @Then("the player should still be alive.")
    public void the_player_should_still_be_alive() {
        Optional<Entity> player = getLocalPlayer();

        assertTrue(player.isPresent());

        Health health = state.getWorld().getEntityManager().getComponentOf(player.get(), Health.class).orElseThrow();
        assertTrue(health.currentHealth > 0);
    }

    @Given("the player is surrounded by follower enemies")
    public void the_player_is_surrounded_by_follower_enemies() {
        state.getWorld()
             .getEntityManager()
             .getEntitiesWith(PlayerTag.class)
             .map(EntityManager.EntityComponentPair::getEntity)
             .map(player -> state.getWorld().getEntityManager().getComponentOf(player, Transform.class).orElseThrow().position)
             .flatMap(playerPosition -> Stream.of(playerPosition.add(2.0, 0.0, new Vector2d()),
                                                  playerPosition.add(-2.0, 0.0, new Vector2d()),
                                                  playerPosition.add(0.0, 2.0, new Vector2d()),
                                                  playerPosition.add(0.0, -2.0, new Vector2d())))
             .forEach(enemyPosition -> FollowerArchetype.create(state.getWorld().getEntityManager(),
                                                                enemyPosition.x,
                                                                enemyPosition.y));
    }

    @Then("the player should be dead.")
    public void the_player_should_be_dead() {
        Optional<Entity> player = getLocalPlayer();

        if (player.isPresent()) {
            Health health = state.getWorld().getEntityManager().getComponentOf(player.get(), Health.class).orElseThrow();
            assertFalse(health.currentHealth > 0);
        }
    }


    private Optional<Entity> getLocalPlayer() {
        return Optional.ofNullable(state.getWorld()
                                        .getResource(Players.class).player);
    }
}
