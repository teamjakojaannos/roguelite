package fi.jakojäännös.roguelite.engine;

import fi.jakojäännös.roguelite.engine.input.InputEvent;
import fi.jakojäännös.roguelite.engine.input.InputProvider;
import fi.jakojäännös.roguelite.engine.input.MouseInfo;
import fi.jakojäännös.roguelite.engine.view.GameRenderer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * Game simulation runner. Utility for running the game simulation.
 *
 * @param <TGame>
 * @param <TInput>
 */
@RequiredArgsConstructor
public abstract class GameRunner<
        TGame extends Game<TState>,
        TInput extends InputProvider,
        TState>
        implements AutoCloseable {
    /**
     * Should the game loop continue running
     *
     * @param game game to check running status for
     *
     * @return <code>true</code> if runner should continue with the game loop. <code>false</code> to
     * break from the loop.
     */
    protected boolean shouldContinueLoop(@NonNull TGame game) {
        return !game.isFinished();
    }

    /**
     * Runs the game. The main entry-point for the game, the first and only call launcher should
     * need to make on the instance.
     *
     * @param game          Game to run
     * @param inputProvider Input provider for gathering input
     * @param renderer      Renderer to use for presenting the game. NOP-renderer is used if
     *                      provided renderer is <code>null</code>.
     */
    public void run(@NonNull Supplier<TState> defaultStateSupplier, @NonNull TGame game, @NonNull TInput inputProvider, GameRenderer<TState> renderer) {
        if (game.isDisposed()) {
            throw new IllegalStateException("Tried running an already disposed game!");
        }

        // Create NOP-renderer if provided renderer is null and we are in the test environment
        GameRenderer<TState> actualRenderer =
                Optional.ofNullable(renderer)
                        .or(() -> Optional.ofNullable(System.getenv("ENVIRONMENT"))
                                          .filter(env -> env.equalsIgnoreCase("test"))
                                          .map(env -> new NOPRenderer()))
                        .orElseThrow(() -> new IllegalStateException("run called outside test environment without specifying a valid renderer!"));

        // Loop
        val state = defaultStateSupplier.get();
        var previousFrameTime = game.getTime().getCurrentTime();
        while (shouldContinueLoop(game)) {
            if (game.isDisposed()) {
                throw new IllegalStateException("Running the loop for already disposed game!");
            }

            val currentFrameTime = game.getTime().getCurrentTime();
            val frameElapsedTime = currentFrameTime - previousFrameTime;
            val delta = frameElapsedTime / 1000.0;
            previousFrameTime = currentFrameTime;

            simulateTick(state, game, inputProvider.pollEvents(), delta);
            presentGameState(state, actualRenderer, delta);
        }
    }

    /**
     * Simulates the game for a single tick.
     *
     * @param game        Game to simulate
     * @param inputEvents Input events to process during this tick
     * @param delta       Time elapsed since the last tick
     */
    public void simulateTick(TState state, TGame game, Queue<InputEvent> inputEvents, double delta) {
        game.tick(state, inputEvents, delta);
    }

    /**
     * Presents the current game state to the user.
     *
     * @param state Game state which to present
     * @param delta Time elapsed since the last tick
     */
    public void presentGameState(TState state, GameRenderer<TState> renderer, double delta) {
        renderer.render(state, delta);
    }

    private class NOPRenderer implements GameRenderer<TState> {
        @Override
        public void render(TState game, double delta) {
        }

        @Override
        public void close() {
        }
    }
}
