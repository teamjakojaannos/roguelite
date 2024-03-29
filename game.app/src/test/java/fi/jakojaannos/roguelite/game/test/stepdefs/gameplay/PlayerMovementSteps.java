package fi.jakojaannos.roguelite.game.test.stepdefs.gameplay;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.components.CharacterStats;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.test.global.GlobalGameState;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.joml.Vector2d;

import static fi.jakojaannos.roguelite.game.test.global.GlobalState.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class PlayerMovementSteps {
    private static final double NO_MOVEMENT_EPSILON = 0.001;
    private static final double APPROXIMATE_DISTANCE_EPSILON = 0.25;

    @Given("the player max speed is {double}, acceleration is {double} and friction is {double}")
    public void the_player_max_speed_is_and_acceleration_is(
            double speed,
            double acceleration,
            double friction
    ) {
        Entity player = GlobalGameState.getLocalPlayer().orElseThrow();
        CharacterStats stats = getComponentOf(player, CharacterStats.class).orElseThrow();
        stats.speed = speed;
        stats.acceleration = acceleration;
        stats.friction = friction;
    }


    @Then("the player should have moved approximately {double} units total on the {string} axis")
    public void the_player_should_have_moved_approximately_x_units_total_on_axis(
            double distance,
            String axis
    ) {
        Entity player = GlobalGameState.getLocalPlayer().orElseThrow();
        Transform transform = getComponentOf(player, Transform.class).orElseThrow();

        Vector2d delta = transform.position.sub(GlobalGameState.playerInitialPosition, new Vector2d());

        boolean horizontal = axis.equalsIgnoreCase("horizontal");

        double translationOnAxis = Math.abs(horizontal ? delta.x : delta.y);
        assertEquals(distance, translationOnAxis, APPROXIMATE_DISTANCE_EPSILON);
    }

    @Then("the player should not have moved at all on the axis {string}")
    public void the_player_should_not_have_moved_at_all_on_the_axis(String axis) {
        Entity player = GlobalGameState.getLocalPlayer().orElseThrow();
        Transform transform = getComponentOf(player, Transform.class).orElseThrow();

        boolean horizontal = axis.equalsIgnoreCase("horizontal");
        double posOnAxis = horizontal ? transform.position.x : transform.position.y;
        double initialPosOnAxis = horizontal ? GlobalGameState.playerInitialPosition.x : GlobalGameState.playerInitialPosition.y;
        assertEquals(initialPosOnAxis, posOnAxis, NO_MOVEMENT_EPSILON);
    }

    @Then("the player should have moved on the {string} axis while the game ran for the last time")
    public void the_player_should_have_moved_on_the_axis(String axis) {
        Entity player = GlobalGameState.getLocalPlayer().orElseThrow();
        Transform transform = getComponentOf(player, Transform.class).orElseThrow();

        boolean horizontal = axis.equalsIgnoreCase("horizontal");
        double posOnAxis = horizontal ? transform.position.x : transform.position.y;
        double initialPosOnAxis = horizontal ? GlobalGameState.playerPositionBeforeRun.x : GlobalGameState.playerPositionBeforeRun.y;
        assertNotEquals(initialPosOnAxis, posOnAxis, NO_MOVEMENT_EPSILON);
    }

    @Then("the player should not have moved at all while the game ran for the last time")
    public void the_player_should_not_have_moved_at_all() {
        Entity player = GlobalGameState.getLocalPlayer().orElseThrow();
        Transform transform = getComponentOf(player, Transform.class).orElseThrow();

        assertEquals(GlobalGameState.playerPositionBeforeRun.x, transform.position.x, NO_MOVEMENT_EPSILON);
        assertEquals(GlobalGameState.playerPositionBeforeRun.y, transform.position.y, NO_MOVEMENT_EPSILON);
    }
}
