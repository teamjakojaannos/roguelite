package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.state.GameState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Game simulation runner. Utility for running the game simulation.
 *
 * @param <TGame>
 * @param <TInput>
 */
@Slf4j
@RequiredArgsConstructor
public abstract class GameRunner<
        TGame extends Game,
        TInput extends InputProvider>
        implements AutoCloseable {
    /**
     * Should the game loop continue running
     *
     * @param game game to check running status for
     *
     * @return <code>true</code> if runner should continue with the game loop. <code>false</code> to
     * break from the loop.
     */
    protected boolean shouldContinueLoop(TGame game) {
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
    public void run(
            final Supplier<GameState> defaultStateSupplier,
            final TGame game,
            final TInput inputProvider,
            final BiConsumer<GameState, Double> renderer
    ) {
        if (game.isDisposed()) {
            throw new IllegalStateException("Tried running an already disposed game!");
        }

        LOG.info("Runner starting...");

        // Loop
        var state = defaultStateSupplier.get();
        val initialTime = System.currentTimeMillis();
        var previousFrameTime = initialTime;
        var accumulator = 0L;
        var ticks = 0;
        var frames = 0;

        LOG.info("Entering main loop");
        while (shouldContinueLoop(game)) {
            val currentFrameTime = System.currentTimeMillis();
            var frameElapsedTime = currentFrameTime - previousFrameTime;
            if (frameElapsedTime > 250L) {
                LOG.warn("Last tick took over 250 ms! Slowing down simulation to catch up!");
                frameElapsedTime = 250L;
            }

            previousFrameTime = currentFrameTime;

            accumulator += frameElapsedTime;
            while (accumulator >= state.getTime().getTimeStep()) {
                state = simulateTick(state, game, inputProvider.pollEvents());
                state.updateTime();
                accumulator -= state.getTime().getTimeStep();
                ++ticks;
            }

            val partialTickAlpha = accumulator / (double) state.getTime().getTimeStep();
            renderer.accept(state, partialTickAlpha);
            presentGameState(state, renderer, partialTickAlpha);
            frames++;
        }

        val totalTime = System.currentTimeMillis() - initialTime;
        val totalTimeSeconds = totalTime / 1000.0;

        val avgTimePerTick = totalTime / (double) ticks;
        val avgTicksPerSecond = ticks / totalTimeSeconds;

        val avgTimePerFrame = totalTime / (double) frames;
        val avgFramesPerSecond = frames / totalTimeSeconds;
        LOG.info("Finished execution after {} seconds", totalTimeSeconds);
        LOG.info("\tTicks:\t\t{}", ticks);
        LOG.info("\tAvg. TPT:\t{}", avgTimePerTick);
        LOG.info("\tAvg. TPS:\t{}", avgTicksPerSecond);
        LOG.info("\tFrames:\t\t{}", frames);
        LOG.info("\tAvg. TPF:\t{}", avgTimePerFrame);
        LOG.info("\tAvg. FPS:\t{}", avgFramesPerSecond);
    }

    /**
     * Simulates the game for a single tick.
     *
     * @param game        Game to simulate
     * @param inputEvents Input events to process during this tick
     */
    public GameState simulateTick(
            final GameState state,
            final TGame game,
            final Queue<InputEvent> inputEvents
    ) {
        if (game.isDisposed()) {
            throw new IllegalStateException("Simulating tick for already disposed game!");
        }

        return game.tick(state, inputEvents);
    }

    /**
     * Presents the current game state to the user.
     *
     * @param state            Game state which to present
     * @param partialTickAlpha Time blending factor between the last two frames we should render at
     */
    public void presentGameState(
            GameState state,
            BiConsumer<GameState, Double> renderer,
            double partialTickAlpha
    ) {
        renderer.accept(state, partialTickAlpha);
    }
}
