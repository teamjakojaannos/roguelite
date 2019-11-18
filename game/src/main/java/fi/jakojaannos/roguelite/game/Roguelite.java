package fi.jakojaannos.roguelite.game;

import fi.jakojaannos.roguelite.engine.GameBase;
import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.input.ButtonInput;
import fi.jakojaannos.roguelite.engine.input.InputAxis;
import fi.jakojaannos.roguelite.engine.input.InputButton;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.systems.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Queue;
import java.util.Random;

@Slf4j
public class Roguelite extends GameBase<GameState> {
    private final SystemDispatcher<GameState> dispatcher;

    public Roguelite() {
        this.dispatcher = new DispatcherBuilder<GameState>()
                .withSystem("player_input", new PlayerInputSystem())
                .withSystem("projectile_move", new ProjectileMovementSystem())
                .withSystem("character_move", new CharacterMovementSystem(), "player_input")
                .withSystem("character_attack", new CharacterAttackSystem(), "player_input")
                .withSystem("crosshair_snap_to_cursor", new SnapToCursorSystem())
                .withSystem("ai_move", new CharacterAIControllerSystem(), "character_move")
                .withSystem("stalker_move", new StalkerAIControllerSystem())
                .build();
    }

    public static GameState createInitialState() {
        val state = new GameState(new Cluster(256, 32));
        state.player = state.world.createEntity();
        state.world.addComponentTo(state.player, new Transform(4.0f, 4.0f));
        state.world.addComponentTo(state.player, new Velocity());
        state.world.addComponentTo(state.player, new CharacterInput());
        state.world.addComponentTo(state.player, new CharacterAbilities());
        state.world.addComponentTo(state.player, new CharacterStats(
                10.0f,
                100.0f,
                150.0f,
                20.0f,
                20.0f
        ));
        state.world.addComponentTo(state.player, new PlayerTag());
        val sprite = new SpriteInfo();
        sprite.spriteName = "textures/sheep.png";
        state.world.addComponentTo(state.player, sprite);

        state.crosshair = state.world.createEntity();
        state.world.addComponentTo(state.crosshair, new Transform(-999.0, -999.0, 0.5, 0.5, 0.25, 0.25));
        state.world.addComponentTo(state.crosshair, new CrosshairTag());


        // Spawn "followers"
        final double x_max = 20.0f, y_max = 15.0f;
        Random random = new Random(123);

        for (int i = 0; i < 0; i++) {
            var e = state.world.createEntity();
            double xpos = random.nextDouble() * x_max;
            double ypos = random.nextDouble() * y_max;
            state.world.addComponentTo(e, new Transform(xpos, ypos));
            state.world.addComponentTo(e, new Velocity());
            state.world.addComponentTo(e, new CharacterInput());
            state.world.addComponentTo(e, new CharacterStats(
                    4.0,
                    100.0,
                    800.0,
                    4.0,
                    20.0
            ));

            state.world.addComponentTo(e, new EnemyAI(25.0f, 1.0f));
        }


        // Spawn stalker(s)
        var e = state.world.createEntity();
        state.world.addComponentTo(e, new Transform(10.0f, 15.0f, 0.75f));
        state.world.addComponentTo(e, new Velocity());
        state.world.addComponentTo(e, new CharacterInput());
        state.world.addComponentTo(e, new CharacterStats(
                1.0,
                100.0,
                800.0,
                4.0,
                20.0
        ));
        state.world.addComponentTo(e,
                new StalkerAI(250.0f, 50.0f, 8.0f));


        state.world.applyModifications();
        return state;
    }

    @Override
    public void tick(
            @NonNull GameState state,
            @NonNull Queue<InputEvent> inputEvents,
            double delta
    ) {
        super.tick(state, inputEvents, delta);
        state.world.applyModifications();

        while (!inputEvents.isEmpty()) {
            val event = inputEvents.remove();

            event.getAxis().ifPresent(input -> {
                if (input.getAxis() == InputAxis.Mouse.X_POS) {
                    state.mouseX = input.getValue();
                } else if (input.getAxis() == InputAxis.Mouse.Y_POS) {
                    state.mouseY = input.getValue();
                }
            });

            event.getButton().ifPresent(input -> {
                if (input.getButton() == InputButton.Keyboard.KEY_A) {
                    state.inputLeft = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_D) {
                    state.inputRight = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_W) {
                    state.inputUp = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_S) {
                    state.inputDown = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Mouse.button(0)) {
                    state.inputAttack = input.getAction() != ButtonInput.Action.RELEASE;
                }
            });
        }

        this.dispatcher.dispatch(state.world, state, delta);
    }
}
