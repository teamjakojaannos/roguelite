package fi.jakojäännös.roguelite.engine.lwjgl.view;

import fi.jakojäännös.roguelite.engine.view.Window;
import lombok.Getter;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Optional;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LWJGLWindow implements Window, AutoCloseable {
    @Getter
    private final long id;

    public LWJGLWindow() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        this.id = glfwCreateWindow(300, 300, "Konna", NULL, NULL);
        if (this.id == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(this.id, pWidth, pHeight);

            Optional.ofNullable(glfwGetVideoMode(glfwGetPrimaryMonitor()))
                    .ifPresent(videoMode -> {
                        glfwSetWindowPos(
                                this.id,
                                (videoMode.width() - pWidth.get(0)) / 2,
                                (videoMode.height() - pHeight.get(0)) / 2);
                    });
        }
    }


    @Override
    public void setResizeCallback(ResizeCallback callback) {
        GLFWWindowSizeCallback
                .create((window, width, height) -> callback.call(width, height))
                .set(this.id);
    }

    @Override
    public void close() {
        glfwFreeCallbacks(this.id);
        glfwDestroyWindow(this.id);

        Optional.ofNullable(glfwSetErrorCallback(null))
                .ifPresent(Callback::free);
    }
}
