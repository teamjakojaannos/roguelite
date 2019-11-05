package fi.jakojäännös.launcher;

import fi.jakojäännös.roguelite.engine.view.GameRenderer;
import fi.jakojäännös.roguelite.game.Roguelite;
import fi.jakojäännös.roguelite.engine.lwjgl.input.LWJGLInputProvider;
import fi.jakojäännös.roguelite.engine.lwjgl.LWJGLGameRunner;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;

@Slf4j
public class Main {
    public static void main(String[] args) {
        val debugStackTraces = true;

        try (val runner = new LWJGLGameRunner<Roguelite, LWJGLInputProvider>()) {
            val inputProvider = new LWJGLInputProvider();
            val game = new Roguelite();
            val renderer = (GameRenderer<Roguelite>) null;
            runner.run(game, inputProvider, renderer);
        } catch (Exception e) {
            LOG.error("The game loop unexpectedly stopped.");
            LOG.error("\tCause: {}", (e.getCause() != null ? e.getCause().toString() : "Not defined."));
            LOG.error("\tMessage: {}", e.getMessage());

            if (debugStackTraces) {
                LOG.error("\tStackTrace:\n{}",
                        Arrays.stream(e.getStackTrace())
                                .map(StackTraceElement::toString)
                                .reduce(e.toString(),
                                        (accumulator, element) -> String.format("%s\n\t%s", accumulator, element)));
            }
        }
    }
}
