package fi.jakojäännös.roguelite.engine;

import fi.jakojäännös.roguelite.engine.input.InputEvent;
import fi.jakojäännös.roguelite.engine.input.InputProvider;
import fi.jakojäännös.roguelite.engine.view.GameRenderer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Queue;

/**
 * Game simulation runner. Utility for running the game simulation.
 *
 * @param <TGame>
 * @param <TInput>
 */
@RequiredArgsConstructor
public abstract class GameRunner<
        TGame extends Game,
        TInput extends InputProvider>
        implements AutoCloseable {
    /**
     * Runs the game. The main entry-point for the game, the first and only call launcher should
     * need to make on the instance.
     *
     * @param game          Game to run
     * @param inputProvider Input provider for gathering input
     * @param renderer      Renderer to use for presenting the game. NOP-renderer is used if
     *                      provided renderer is <code>null</code>.
     */
    public void run(@NonNull TGame game, @NonNull TInput inputProvider, GameRenderer<TGame> renderer) {
        if (game.isDisposed()) {
            throw new IllegalStateException("Tried running an already disposed game!");
        }

        // Create NOP-renderer if provided renderer is null
        GameRenderer<TGame> actualRenderer = renderer != null
                ? renderer
                : (g, d) -> {/* NOP */};

        // Loop
        var previousFrameTime = game.getTime().getCurrentTime();
        while (!game.isFinished()) {
            if (game.isDisposed()) {
                throw new IllegalStateException("Running the loop for already disposed game!");
            }

            val currentFrameTime = game.getTime().getCurrentTime();
            val frameElapsedTime = currentFrameTime - previousFrameTime;
            val delta = frameElapsedTime / 1000.0;
            previousFrameTime = currentFrameTime;

            simulateTick(game, inputProvider.pollEvents(), delta);
            presentGameState(game, actualRenderer, delta);
        }
    }

    /**
     * Simulates the game for a single tick.
     *
     * @param game        Game to simulate
     * @param inputEvents Input events to process during this tick
     * @param delta       Time elapsed since the last tick
     */
    public void simulateTick(TGame game, Queue<InputEvent> inputEvents, double delta) {
        game.tick(inputEvents, delta);
    }

    /**
     * Presents the current game state to the user.
     *
     * @param game  Game which state to present
     * @param delta Time elapsed since the last tick
     */
    public void presentGameState(TGame game, GameRenderer<TGame> renderer, double delta) {
        renderer.render(game, delta);
    }
}
