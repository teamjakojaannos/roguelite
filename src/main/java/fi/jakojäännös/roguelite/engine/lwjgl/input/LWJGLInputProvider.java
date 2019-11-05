package fi.jakojäännös.roguelite.engine.lwjgl.input;

import fi.jakojäännös.roguelite.engine.input.InputEvent;
import fi.jakojäännös.roguelite.engine.input.InputProvider;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class LWJGLInputProvider extends InputProvider {
    private final long windowId;
    private final Queue<InputEvent> inputEvents;

    public LWJGLInputProvider(long windowId, boolean enableForceClose) {
        this.windowId = windowId;
        this.inputEvents = new ArrayDeque<>();

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
            // TODO: Convert key/scancode to some enum
            this.inputEvents.offer(new InputEvent(key, scancode, inputAction));

            if (enableForceClose && key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(windowId, true);
            }
        });
    }
}
