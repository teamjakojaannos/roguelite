package fi.jakojäännös.roguelite.engine.lwjgl;

import fi.jakojäännös.roguelite.engine.Game;
import fi.jakojäännös.roguelite.engine.GameRunner;
import fi.jakojäännös.roguelite.engine.input.InputProvider;
import fi.jakojäännös.roguelite.engine.lwjgl.view.LWJGLWindow;
import fi.jakojäännös.roguelite.engine.view.GameRenderer;
import lombok.Getter;
import lombok.NonNull;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class LWJGLGameRunner<TGame extends Game, TInput extends InputProvider> extends GameRunner<TGame, TInput> {
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
    public void presentGameState(TGame game, GameRenderer<TGame> renderer, double delta) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        super.presentGameState(game, renderer, delta);

        glfwSwapBuffers(this.window.getId());
        glfwPollEvents();
    }

    @Override
    public void close() {
        this.window.close();
        glfwTerminate();
    }
}
