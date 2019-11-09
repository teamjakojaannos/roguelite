package fi.jakojäännös.roguelite.game;

import fi.jakojäännös.roguelite.engine.GameBase;
import fi.jakojäännös.roguelite.engine.ecs.*;
import fi.jakojäännös.roguelite.game.data.GameState;
import fi.jakojäännös.roguelite.engine.input.ButtonInput;
import fi.jakojäännös.roguelite.engine.input.InputAxis;
import fi.jakojäännös.roguelite.engine.input.InputButton;
import fi.jakojäännös.roguelite.engine.input.InputEvent;
import fi.jakojäännös.roguelite.game.data.components.CrosshairTag;
import fi.jakojäännös.roguelite.game.data.components.PlayerTag;
import fi.jakojäännös.roguelite.game.data.components.Position;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

@Slf4j
public class Roguelite extends GameBase<GameState> {
    private SystemDispatcher dispatcher = new SystemDispatcher(
            List.of(new ECSSystem() {
                        @Override
                        public Collection<Class<? extends Component>> getRequiredComponents() {
                            return List.of(Position.class, PlayerTag.class);
                        }

                        @Override
                        public void tick(Stream<Entity> entities, GameState state, double delta) {
                            val playerDirectionMultiplierX = (state.inputRight ? 1 : 0) - (state.inputLeft ? 1 : 0);
                            val playerDirectionMultiplierY = (state.inputDown ? 1 : 0) - (state.inputUp ? 1 : 0);
                            val playerVelocityX = state.playerSpeed * playerDirectionMultiplierX;
                            val playerVelocityY = state.playerSpeed * playerDirectionMultiplierY;

                            entities.forEach(entity -> state.world.getComponentOf(entity, Position.class)
                                                                  .ifPresent(position -> {
                                                                      position.x += playerVelocityX * delta;
                                                                      position.y += playerVelocityY * delta;
                                                                  }));
                        }
                    },
                    new ECSSystem() {
                        @Override
                        public Collection<Class<? extends Component>> getRequiredComponents() {
                            return List.of(Position.class, CrosshairTag.class);
                        }

                        @Override
                        public void tick(Stream<Entity> entities, GameState state, double delta) {
                            entities.forEach(entity -> state.world.getComponentOf(entity, Position.class)
                                                                  .ifPresent(position -> {
                                                                      position.x = state.mouseX * state.realViewWidth;
                                                                      position.y = state.mouseY * state.realViewHeight;
                                                                  }));
                        }
                    })
    );

    public GameState createInitialState() {
        val state = new GameState();
        state.world = new Cluster(256);
        state.world.registerComponentType(Position.class, Position[]::new);
        state.world.registerComponentType(PlayerTag.class, PlayerTag[]::new);
        state.world.registerComponentType(CrosshairTag.class, CrosshairTag[]::new);

        state.player = state.world.createEntity();
        state.world.addComponentTo(state.player, new Position(4.0f, 4.0f));
        state.world.addComponentTo(state.player, new PlayerTag());

        state.crosshair = state.world.createEntity();
        state.world.addComponentTo(state.crosshair, new Position(-999.0f, -999.0f));
        state.world.addComponentTo(state.crosshair, new CrosshairTag());


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
                }
            });
        }

        this.dispatcher.dispatch(state.world, state, delta);
    }
}
