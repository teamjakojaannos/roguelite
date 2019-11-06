package fi.jakojäännös.roguelite.engine.lwjgl.input;

import fi.jakojäännös.roguelite.engine.input.InputEvent;
import fi.jakojäännös.roguelite.engine.input.InputProvider;
import fi.jakojäännös.roguelite.engine.lwjgl.view.LWJGLWindow;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

@Slf4j
public class LWJGLInputProvider extends InputProvider {
    private final Queue<InputEvent> inputEvents;

    public LWJGLInputProvider(LWJGLWindow lwjglWindow, boolean enableForceClose) {
        this.inputEvents = new ArrayDeque<>();

        val windowId = lwjglWindow.getId();
        glfwSetKeyCallback(windowId, (window, key, scancode, action, mods) -> {
            InputEvent.Action inputAction;
            switch (action) {
                case GLFW_RELEASE:
                    inputAction = InputEvent.Action.RELEASE;
                    break;
                case GLFW_PRESS:
                    inputAction = InputEvent.Action.PRESS;
                    break;
                case GLFW_REPEAT:
                    inputAction = InputEvent.Action.REPEAT;
                    break;
                default:
                    // TODO: Handle errors/repeat actions
                    return;
            }
            // TODO: Convert key/scancode to some more sensible data-structure
            this.inputEvents.offer(new InputEvent(key, scancode, inputAction));

            if (enableForceClose && key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                LOG.info("Received force close signal. Sending WindowShouldClose notify.");
                glfwSetWindowShouldClose(windowId, true);
            }
        });
    }
}
