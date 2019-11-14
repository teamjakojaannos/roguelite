package fi.jakojaannos.roguelite.launcher;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;

/**
 * The main entry point. Passes command-line arguments to the launcher and proceeds to launch.
 */
@Slf4j
public class Main {
    public static void main(String[] args) {
        LOG.trace("Got command-line arguments: {}", Arrays.toString(args));

        val launcher = new RogueliteLauncher();
        launcher.parseCommandLineArguments(args);
        launcher.launch();
    }
}
