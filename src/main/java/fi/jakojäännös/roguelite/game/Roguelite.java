package fi.jakojäännös.roguelite.game;

import fi.jakojäännös.roguelite.engine.Game;
import fi.jakojäännös.roguelite.engine.input.InputEvent;
import fi.jakojäännös.roguelite.utilities.TimeManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Optional;
import java.util.Queue;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
public class Roguelite implements Game {
    private long windowId;

    public void run() {
        LOG.info("Hello world!");

        init();
        loop();

        glfwFreeCallbacks(windowId);
        glfwDestroyWindow(windowId);

        glfwTerminate();
        Optional.ofNullable(glfwSetErrorCallback(null))
                .ifPresent(Callback::free);
    }

    private void init() {
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

        glfwSetKeyCallback(windowId, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(windowId, true);
            }
        });

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
    }

    private void loop() {
        GL.createCapabilities();
        glClearColor(0.25f, 0.6f, 0.4f, 1.0f);
        glViewport(0, 0, 1, 1);
        glMatrixMode(GL_PROJECTION);

        glLoadIdentity();

        while (!glfwWindowShouldClose(windowId)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glTranslatef(0.0f, 0.0f, -4.0f);

            glColor4f(1.0f, 0.0f, 0.0f, 1.0f);

            glBegin(GL_TRIANGLES);
            glVertex2f(0.0f, 0.0f);
            glVertex2f(1.0f, 1.0f);
            glVertex2f(1.0f, 0.0f);

            glVertex2f(0.0f, 0.0f);
            glVertex2f(1.0f, 1.0f);
            glVertex2f(0.0f, 1.0f);
            glEnd();

            glfwSwapBuffers(windowId);
            glfwPollEvents();
        }
    }

    @Override
    public TimeManager getTime() {
        return null;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void setFinished(boolean state) {

    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void tick(Queue<InputEvent> inputEvents, double delta) {

    }
}
