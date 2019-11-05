package fi.jakojäännös.roguelite.engine.lwjgl;

import fi.jakojäännös.roguelite.engine.Game;
import fi.jakojäännös.roguelite.engine.GameRunner;
import fi.jakojäännös.roguelite.engine.input.InputProvider;
import fi.jakojäännös.roguelite.engine.view.GameRenderer;
import lombok.Getter;
import lombok.NonNull;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Optional;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LWJGLGameRunner<TGame extends Game, TInput extends InputProvider> extends GameRunner<TGame, TInput> {
    @Getter
    private final long windowId;

    public LWJGLGameRunner() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        windowId = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
        if (windowId == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(windowId, pWidth, pHeight);

            Optional.ofNullable(glfwGetVideoMode(glfwGetPrimaryMonitor()))
                    .ifPresent(videoMode -> {
                        glfwSetWindowPos(
                                windowId,
                                (videoMode.width() - pWidth.get(0)) / 2,
                                (videoMode.height() - pHeight.get(0)) / 2);
                    });
        }

        glfwMakeContextCurrent(windowId);
        glfwSwapInterval(1);
        glfwShowWindow(windowId);

        GL.createCapabilities();
        glClearColor(0.25f, 0.6f, 0.4f, 1.0f);
    }

    @Override
    protected boolean shouldContinueLoop(@NonNull TGame game) {
        return super.shouldContinueLoop(game) && !glfwWindowShouldClose(windowId);
    }

    @Override
    public void presentGameState(TGame game, GameRenderer<TGame> renderer, double delta) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        super.presentGameState(game, renderer, delta);

        glfwSwapBuffers(windowId);
        glfwPollEvents();
    }

    @Override
    public void close() throws Exception {
        glfwFreeCallbacks(windowId);
        glfwDestroyWindow(windowId);

        glfwTerminate();
        Optional.ofNullable(glfwSetErrorCallback(null))
                .ifPresent(Callback::free);
    }
}
