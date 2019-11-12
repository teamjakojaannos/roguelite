package fi.jakojäännös.roguelite.engine.lwjgl.view.rendering;

import fi.jakojäännös.roguelite.engine.utilities.io.TextFileHelper;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

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
            @NonNull String fragmentShaderPath
    ) {
        this.vertexShader = glCreateShader(GL_VERTEX_SHADER);
        this.fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        this.shaderProgram = glCreateProgram();

        try {
            glShaderSource(this.vertexShader,
                           TextFileHelper.readFileToString(vertexShaderPath));
        } catch (IOException e) {
            LOG.error("Loading sprite fragment shader failed!");
            return;
        }
        glCompileShader(this.vertexShader);

        try {
            glShaderSource(this.fragmentShader,
                           TextFileHelper.readFileToString(fragmentShaderPath));
        } catch (IOException e) {
            LOG.error("Loading sprite fragment shader failed!");
            return;
        }
        glCompileShader(this.fragmentShader);

        if (glGetShaderi(this.vertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            LOG.error("Error compiling vertex shader:\n{}", glGetShaderInfoLog(this.vertexShader));
        }

        if (glGetShaderi(this.fragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
            LOG.error("Error compiling fragment shader:\n{}", glGetShaderInfoLog(this.fragmentShader));
        }

        glAttachShader(this.shaderProgram, this.vertexShader);
        glAttachShader(this.shaderProgram, this.fragmentShader);

        glBindAttribLocation(this.shaderProgram, 0, "in_pos");
        glBindFragDataLocation(this.shaderProgram, 0, "out_fragColor");

        glLinkProgram(this.shaderProgram);

        if (glGetProgrami(this.shaderProgram, GL_LINK_STATUS) != GL_TRUE) {
            LOG.error(glGetProgramInfoLog(this.shaderProgram));
        }
    }

    @Override
    public void close() {
        glDeleteShader(this.vertexShader);
        glDeleteShader(this.fragmentShader);
        glDeleteProgram(this.shaderProgram);
    }
}
