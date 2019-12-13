package fi.jakojaannos.roguelite.engine.lwjgl;

import fi.jakojaannos.roguelite.engine.Game;
import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLWindow;
import lombok.Getter;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.util.function.BiConsumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class LWJGLGameRunner<TGame extends Game<TState>, TInput extends InputProvider, TState>
        extends GameRunner<TGame, TInput, TState> {
    @Getter
    private final LWJGLWindow window;

    public LWJGLGameRunner(int windowWidth, int windowHeight, boolean floatWindow) {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        this.window = new LWJGLWindow(
                windowWidth == -1 ? 800 : windowWidth,
                windowHeight == -1 ? 600 : windowHeight,
                floatWindow
        );

        glfwMakeContextCurrent(this.window.getId());
        glfwSwapInterval(1);
        glfwShowWindow(this.window.getId());

        if (floatWindow) {
            glfwSetWindowAttrib(this.window.getId(), GLFW_FLOATING, GLFW_TRUE);
        }

        GL.createCapabilities();
        glClearColor(0.25f, 0.6f, 0.4f, 1.0f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    protected boolean shouldContinueLoop(TGame game) {
        return super.shouldContinueLoop(game) && !glfwWindowShouldClose(this.window.getId());
    }

    @Override
    public void presentGameState(
            TState state,
            BiConsumer<TState, Double> renderer,
            double partialTickAlpha
    ) {
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
