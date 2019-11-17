package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering;

import fi.jakojaannos.roguelite.engine.utilities.io.TextFileHelper;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;

@Slf4j
public class ShaderProgram implements AutoCloseable {
    @Getter private final int shaderProgram;
    private final int vertexShader;
    private final int fragmentShader;

    public ShaderProgram(
            @NonNull String vertexShaderPath,
            @NonNull String fragmentShaderPath,
            @NonNull Map<Integer, String> attributeLocations,
            @NonNull Map<Integer, String> fragDataLocations
    ) {
        this.vertexShader = glCreateShader(GL_VERTEX_SHADER);
        this.fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        this.shaderProgram = glCreateProgram();

        // Compile the vertex shader
        try {
            GL20.glShaderSource(this.vertexShader,
                                TextFileHelper.readFileToString(vertexShaderPath));
        } catch (IOException e) {
            LOG.error("Loading vertex shader \"{}\" failed!", vertexShaderPath);
            return;
        }
        glCompileShader(this.vertexShader);

        // Compile the fragment shader
        try {
            glShaderSource(this.fragmentShader,
                           TextFileHelper.readFileToString(fragmentShaderPath));
        } catch (IOException e) {
            LOG.error("Loading fragment shader \"{}\" failed!", fragmentShaderPath);
            return;
        }
        glCompileShader(this.fragmentShader);

        // Check for errors
        if (glGetShaderi(this.vertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            LOG.error("Error compiling vertex shader:\n{}", glGetShaderInfoLog(this.vertexShader));
        }
        if (glGetShaderi(this.fragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
            LOG.error("Error compiling fragment shader:\n{}", glGetShaderInfoLog(this.fragmentShader));
        }

        // Attach shaders and bind data locations
        glAttachShader(this.shaderProgram, this.vertexShader);
        glAttachShader(this.shaderProgram, this.fragmentShader);
        attributeLocations.forEach((index, name) -> glBindAttribLocation(this.shaderProgram, index, name));
        fragDataLocations.forEach((colorNumber, name) -> glBindFragDataLocation(this.shaderProgram, colorNumber, name));

        // Link the program and check for errors
        glLinkProgram(this.shaderProgram);
        if (glGetProgrami(this.shaderProgram, GL_LINK_STATUS) != GL_TRUE) {
            LOG.error(glGetProgramInfoLog(this.shaderProgram));
        }
    }

    public void use() {
        glUseProgram(this.shaderProgram);
    }

    public void setUniformMat4x4(int uniformLocation, Matrix4f matrix) {
        try (val stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(uniformLocation, false, matrix.get(stack.mallocFloat(16)));
        }
    }

    public int getUniformLocation(String name) {
        return glGetUniformLocation(shaderProgram, name);
    }

    @Override
    public void close() {
        glDeleteShader(this.vertexShader);
        glDeleteShader(this.fragmentShader);
        glDeleteProgram(this.shaderProgram);
    }
}
