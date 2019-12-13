package fi.jakojaannos.roguelite.engine;

import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
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
            final Supplier<TState> defaultStateSupplier,
            final TGame game,
            final TInput inputProvider,
            final BiConsumer<TState, Double> renderer
    ) {
        if (game.isDisposed()) {
            throw new IllegalStateException("Tried running an already disposed game!");
        }

        LOG.info("Runner starting...");

        // Loop
        val state = defaultStateSupplier.get();
        game.getTime().refresh();
        val initialTime = System.currentTimeMillis();
        var previousFrameTime = game.getTime().getCurrentRealTime();
        var accumulator = 0L;
        var ticks = 0;
        var frames = 0;

        val simulationTimestep = 20L; // 50 TPS = 20ms per tick
        val simulationTimestepInSeconds = simulationTimestep / 1000.0;

        LOG.info("Entering main loop");
        while (shouldContinueLoop(game)) {
            game.getTime().refresh();
            val currentFrameTime = game.getTime().getCurrentRealTime();
            var frameElapsedTime = currentFrameTime - previousFrameTime;
            if (frameElapsedTime > 250L) {
                LOG.warn("Last tick took over 250 ms! Slowing down simulation to catch up!");
                frameElapsedTime = 250L;
            }

            previousFrameTime = currentFrameTime;

            accumulator += frameElapsedTime;
            while (accumulator >= simulationTimestep) {
                simulateTick(state, game, inputProvider.pollEvents(), simulationTimestepInSeconds);

                game.getTime().progressGameTime(simulationTimestep);
                accumulator -= simulationTimestep;
                ++ticks;
            }

            val partialTickAlpha = accumulator / (double) simulationTimestep;
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
     * @param delta       Time since the last tick
     */
    public void simulateTick(
            TState state,
            TGame game,
            Queue<InputEvent> inputEvents,
            double delta
    ) {
        if (game.isDisposed()) {
            throw new IllegalStateException("Simulating tick for already disposed game!");
        }

        game.tick(state, inputEvents, delta);
    }

    /**
     * Presents the current game state to the user.
     *
     * @param state            Game state which to present
     * @param partialTickAlpha Time blending factor between the last two frames we should render at
     */
    public void presentGameState(
            TState state,
            BiConsumer<TState, Double> renderer,
            double partialTickAlpha
    ) {
        renderer.accept(state, partialTickAlpha);
    }
}
