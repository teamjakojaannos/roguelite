package fi.jakojaannos.roguelite.game.test.stepdefs;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.input.ButtonInput;
import fi.jakojaannos.roguelite.engine.input.InputButton;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.game.Roguelite;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.archetypes.FollowerArchetype;
import fi.jakojaannos.roguelite.game.data.components.*;
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

import static org.junit.jupiter.api.Assertions.*;

public class GameplaySteps {
    private static final double NO_MOVEMENT_EPSILON = 0.001;
    private static final double APPROXIMATE_DISTANCE_EPSILON = 0.25;

    private Roguelite game;
    private GameState state;
    private Queue<InputEvent> inputEvents;

    private Vector2d playerInitialPosition;
    private Vector2d playerPositionBeforeRun;

    @Before
    public void before() {
        game = new Roguelite();
        inputEvents = new ArrayDeque<>();
    }

    @Given("the game world just finished loading")
    public void the_game_world_just_finished_loading() {
        state = Roguelite.createInitialState(6969);
        playerInitialPosition = getLocalPlayer().flatMap(entity -> getComponentOf(entity, Transform.class))
                                                .map(transform -> new Vector2d(transform.position))
                                                .orElseThrow();
        playerPositionBeforeRun = new Vector2d(playerInitialPosition);
    }

    @Given("the player max speed is {double}, acceleration is {double} and friction is {double}")
    public void the_player_max_speed_is_and_acceleration_is(
            double speed,
            double acceleration,
            double friction
    ) {
        Entity player = getLocalPlayer().orElseThrow();
        CharacterStats stats = getComponentOf(player, CharacterStats.class).orElseThrow();
        stats.speed = speed;
        stats.acceleration = acceleration;
        stats.friction = friction;
    }

    @Given("the player has held {string} for {double} seconds")
    public void the_player_has_held_for_seconds(String key, double seconds) {
        updatePlayerPositionBeforeRun();
        pressKey(key);
        simulateSeconds(seconds);
    }

    @Given("the player is surrounded by follower enemies")
    public void the_player_is_surrounded_by_follower_enemies() {
        state.getWorld()
             .getEntityManager()
             .getEntitiesWith(PlayerTag.class)
             .map(EntityManager.EntityComponentPair::getEntity)
             .map(player -> getComponentOf(player, Transform.class).orElseThrow().position)
             .flatMap(playerPosition -> Stream.of(playerPosition.add(2.0, 0.0, new Vector2d()),
                                                  playerPosition.add(-2.0, 0.0, new Vector2d()),
                                                  playerPosition.add(0.0, 2.0, new Vector2d()),
                                                  playerPosition.add(0.0, -2.0, new Vector2d())))
             .forEach(enemyPosition -> FollowerArchetype.spawnFollower(state.getWorld().getEntityManager(),
                                                                       enemyPosition.x,
                                                                       enemyPosition.y));
    }

    @Given("there are no obstacles")
    public void there_are_no_obstacles() {
        state.getWorld()
             .getEntityManager()
             .getEntitiesWith(ObstacleTag.class)
             .map(EntityManager.EntityComponentPair::getEntity)
             .forEach(state.getWorld().getEntityManager()::destroyEntity);
    }

    @Given("there are no spawners")
    public void there_are_no_spawners() {
        state.getWorld()
             .getEntityManager()
             .getEntitiesWith(SpawnerComponent.class)
             .map(EntityManager.EntityComponentPair::getEntity)
             .forEach(state.getWorld().getEntityManager()::destroyEntity);
    }

    @Given("the player has released the key {string}")
    public void the_player_has_released_the_key(String key) {
        releaseKey(key);
    }

    @Given("the game has run for {double} seconds")
    public void the_game_has_run_for_seconds(double seconds) {
        updatePlayerPositionBeforeRun();
        simulateSeconds(seconds);
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

    @When("the game runs for {double} seconds")
    public void the_game_runs_for_x_seconds(double seconds) {
        updatePlayerPositionBeforeRun();
        simulateSeconds(seconds);
    }

    @When("the game runs for a single tick")
    public void the_game_runs_for_a_single_tick() {
        updatePlayerPositionBeforeRun();
        simulateTick();
    }

    @When("player presses key {string}")
    public void player_presses_key(String key) {
        pressKey(key);
    }

    @When("player releases key {string}")
    public void player_releases_key(String key) {
        releaseKey(key);
    }

    @Then("the player should still be alive.")
    public void the_player_should_still_be_alive() {
        Optional<Entity> player = getLocalPlayer();

        assertTrue(player.isPresent());

        Health health = getComponentOf(player.get(), Health.class).orElseThrow();
        assertTrue(health.currentHealth > 0);
    }

    @Then("the player should be dead.")
    public void the_player_should_be_dead() {
        Optional<Entity> player = getLocalPlayer();

        if (player.isPresent()) {
            Health health = state.getWorld().getEntityManager().getComponentOf(player.get(), Health.class).orElseThrow();
            assertFalse(health.currentHealth > 0);
        }
    }

    @Then("the player should have moved approximately {double} units total on the {string} axis")
    public void the_player_should_have_moved_approximately_x_units_total_on_axis(
            double distance,
            String axis
    ) {
        Entity player = getLocalPlayer().orElseThrow();
        Transform transform = getComponentOf(player, Transform.class).orElseThrow();

        Vector2d delta = transform.position.sub(playerInitialPosition, new Vector2d());

        boolean horizontal = axis.equalsIgnoreCase("horizontal");

        double translationOnAxis = Math.abs(horizontal ? delta.x : delta.y);
        assertEquals(distance, translationOnAxis, APPROXIMATE_DISTANCE_EPSILON);
    }

    @Then("the player should not have moved at all on the axis {string}")
    public void the_player_should_not_have_moved_at_all_on_the_axis(String axis) {
        Entity player = getLocalPlayer().orElseThrow();
        Transform transform = getComponentOf(player, Transform.class).orElseThrow();

        boolean horizontal = axis.equalsIgnoreCase("horizontal");
        double posOnAxis = horizontal ? transform.position.x : transform.position.y;
        double initialPosOnAxis = horizontal ? playerInitialPosition.x : playerInitialPosition.y;
        assertEquals(initialPosOnAxis, posOnAxis, NO_MOVEMENT_EPSILON);
    }

    @Then("the player should have moved on the {string} axis while the game ran for the last time")
    public void the_player_should_have_moved_on_the_axis(String axis) {
        Entity player = getLocalPlayer().orElseThrow();
        Transform transform = getComponentOf(player, Transform.class).orElseThrow();

        boolean horizontal = axis.equalsIgnoreCase("horizontal");
        double posOnAxis = horizontal ? transform.position.x : transform.position.y;
        double initialPosOnAxis = horizontal ? playerPositionBeforeRun.x : playerPositionBeforeRun.y;
        assertNotEquals(initialPosOnAxis, posOnAxis, NO_MOVEMENT_EPSILON);
    }

    @Then("the player should not have moved at all while the game ran for the last time")
    public void the_player_should_not_have_moved_at_all() {
        Entity player = getLocalPlayer().orElseThrow();
        Transform transform = getComponentOf(player, Transform.class).orElseThrow();

        assertEquals(playerPositionBeforeRun.x, transform.position.x, NO_MOVEMENT_EPSILON);
        assertEquals(playerPositionBeforeRun.y, transform.position.y, NO_MOVEMENT_EPSILON);
    }

    private Optional<Entity> getLocalPlayer() {
        return Optional.ofNullable(state.getWorld()
                                        .getResource(Players.class).player);
    }

    private <T extends Component> Optional<T> getComponentOf(
            Entity player,
            Class<T> componentClass
    ) {
        return state.getWorld().getEntityManager().getComponentOf(player, componentClass);
    }

    private void simulateTick() {
        game.tick(state, inputEvents, 0.02);
        game.getTime().progressGameTime(20L);
        inputEvents.clear();
    }

    private void simulateSeconds(double seconds) {
        int ticks = (int) (seconds / 0.02);
        for (int i = 0; i < ticks; ++i) {
            simulateTick();
        }
    }

    private void updatePlayerPositionBeforeRun() {
        playerPositionBeforeRun = getLocalPlayer().flatMap(player -> getComponentOf(player, Transform.class))
                                                  .map(transform -> new Vector2d(transform.position))
                                                  .orElseThrow();
    }

    private void pressKey(String key) {
        inputEvents.offer(ButtonInput.pressed(InputButton.Keyboard.valueOf("KEY_" + key.toUpperCase())));
    }

    private void releaseKey(String key) {
        inputEvents.offer(ButtonInput.released(InputButton.Keyboard.valueOf("KEY_" + key.toUpperCase())));
    }
}
