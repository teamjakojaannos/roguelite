package fi.jakojaannos.roguelite.engine.lwjgl;

import fi.jakojaannos.roguelite.engine.Game;
import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLWindow;
import fi.jakojaannos.roguelite.engine.view.GameRenderer;
import lombok.Getter;
import lombok.NonNull;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class LWJGLGameRunner<TGame extends Game<TState>, TInput extends InputProvider, TState>
        extends GameRunner<TGame, TInput, TState> {
    @Getter
    private final LWJGLWindow window;

    public LWJGLGameRunner() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        this.window = new LWJGLWindow();

        glfwMakeContextCurrent(this.window.getId());
        glfwSwapInterval(1);
        glfwShowWindow(this.window.getId());

        GL.createCapabilities();
        glClearColor(0.25f, 0.6f, 0.4f, 1.0f);
    }

    @Override
    protected boolean shouldContinueLoop(@NonNull TGame game) {
        return super.shouldContinueLoop(game) && !glfwWindowShouldClose(this.window.getId());
    }

    @Override
    public void presentGameState(TState state, GameRenderer<TState> renderer, double partialTickAlpha) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        super.presentGameState(state, renderer, partialTickAlpha);

        glfwSwapBuffers(this.window.getId());
        glfwPollEvents();
    }

    @Override
    public void close() {
        this.window.close();
        glfwTerminate();
    }
}
