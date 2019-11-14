package fi.jakojaannos.roguelite.launcher;

import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLGameRunner;
import fi.jakojaannos.roguelite.engine.lwjgl.input.LWJGLInputProvider;
import fi.jakojaannos.roguelite.engine.view.Window;
import fi.jakojaannos.roguelite.game.Roguelite;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.view.RogueliteGameRenderer;
import fi.jakojaannos.roguelite.launcher.arguments.Argument;
import fi.jakojaannos.roguelite.launcher.arguments.ArgumentParsingException;
import fi.jakojaannos.roguelite.launcher.arguments.Arguments;
import fi.jakojaannos.roguelite.launcher.arguments.Parameter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class RogueliteLauncher {
    @Setter private boolean enableLWJGLDebug = false;
    @Setter private boolean enableLWJGLLibraryLoaderDebug = false;
    @Setter private boolean enableForceClose = false;
    @Setter private boolean debugStackTraces = false;
    @Setter private int windowWidth = -1;
    @Setter private int windowHeight = -1;
    @Setter private String assetRoot = "assets/";
    @Setter private Window.Mode windowMode = Window.Mode.Windowed;

    public void parseCommandLineArguments(@NonNull String... args) {
        try {
            Arguments.builder()
                     .with(Argument.withName("window")
                                   .withAction((params) -> {
                                       val width = params.parameter(Parameter.integer("width")
                                                                             .withMin(1));
                                       val height = params.parameter(Parameter.integer("height")
                                                                              .withMin(1));
                                       val mode = params.parameter(
                                               Parameter.optional(Parameter.enumeration("mode",
                                                                                        Window.Mode.class)));

                                       this.setWindowWidth(width);
                                       this.setWindowHeight(height);
                                       mode.ifPresent(this::setWindowMode);
                                   }))
                     .with(Argument.withName("width")
                                   .withAction(params -> {
                                       val width = params.parameter(Parameter.integer("width")
                                                                             .withMin(1));
                                       this.setWindowWidth(width);
                                   }))
                     .with(Argument.withName("height")
                                   .withAction(params -> {
                                       val width = params.parameter(Parameter.integer("height")
                                                                             .withMin(1));
                                       this.setWindowWidth(width);
                                   }))
                     .with(Argument.withName("windowMode")
                                   .withAction(params -> {
                                       val mode = params.parameter(Parameter.enumeration("mode",
                                                                                         Window.Mode.class));
                                       this.setWindowMode(mode);
                                   }))
                     .with(Argument.withName("assetRoot")
                                   .withAction(params -> {
                                       val assetRoot = params.parameter(Parameter.filePath("path")
                                                                                 .mustBeDirectory()
                                                                                 .mustExist());

                                       LOG.debug("Changing asset root");
                                       this.setAssetRoot(assetRoot);
                                   }))
                     .with(Argument.withName("enableLWJGLDebug")
                                   .withAction(params -> this.setEnableLWJGLDebug(true)))
                     .with(Argument.withName("enableLWJGLLibraryLoaderDebug")
                                   .withAction(params -> this.setEnableLWJGLLibraryLoaderDebug(true)))
                     .with(Argument.withName("enableForceClose")
                                   .withAction(params -> this.setEnableForceClose(true)))
                     .with(Argument.withName("debugStackTraces")
                                   .withAction(params -> this.setDebugStackTraces(true)))
                     .ignoreUnknown()
                     .consume(args);
        } catch (ArgumentParsingException e) {
            LOG.error("Error parsing command-line arguments: ", e);
        }

    }

    public void launch() {
        if (!(this.assetRoot.endsWith("/") || this.assetRoot.endsWith("\\"))) {
            this.assetRoot = this.assetRoot + '/';
        }

        if (this.enableLWJGLDebug) {
            LOG.info("Enabling LWJGL Debug mode");
            System.setProperty("org.lwjgl.util.Debug", "true");
        }

        if (this.enableLWJGLLibraryLoaderDebug) {
            LOG.info("Enabling LWJGL SharedLibraryLoader debug mode");
            System.setProperty("org.lwjgl.util.DebugLoader", "true");
        }

        try (val runner = new LWJGLGameRunner<Roguelite, LWJGLInputProvider, GameState>(
                this.windowWidth,
                this.windowHeight
        )) {
            try (val renderer = new RogueliteGameRenderer(
                    this.assetRoot,
                    runner.getWindow());
                 val game = new Roguelite()
            ) {
                val inputProvider = new LWJGLInputProvider(runner.getWindow(), this.enableForceClose);
                runner.run(game::createInitialState, game, inputProvider, renderer);
            }
        } catch (Exception e) {
            LOG.error("The game loop unexpectedly stopped.");
            LOG.error("\tException:\t{}", e.getClass().getName());
            LOG.error("\tAt:\t\t{}:{}", e.getStackTrace()[0].getFileName(), e.getStackTrace()[0].getLineNumber());
            LOG.error("\tCause:\t\t{}", Optional.ofNullable(e.getCause()).map(Throwable::toString).orElse("Cause not defined."));
            LOG.error("\tMessage:\t{}", e.getMessage());

            if (this.debugStackTraces) {
                LOG.error("\tStackTrace:\n{}",
                          Arrays.stream(e.getStackTrace())
                                .map(StackTraceElement::toString)
                                .reduce(e.toString(),
                                        (accumulator, element) -> String.format("%s\n\t%s", accumulator, element)));
            } else {
                LOG.error("\tRun with --debug for stack traces");
            }
        }
    }
}
