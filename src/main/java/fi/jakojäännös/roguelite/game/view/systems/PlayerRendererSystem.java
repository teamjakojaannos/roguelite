package fi.jakojäännös.roguelite.game.view.systems;

import fi.jakojäännös.roguelite.engine.ecs.Cluster;
import fi.jakojäännös.roguelite.engine.ecs.Component;
import fi.jakojäännös.roguelite.engine.ecs.ECSSystem;
import fi.jakojäännös.roguelite.engine.ecs.Entity;
import fi.jakojäännös.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojäännös.roguelite.game.data.GameState;
import fi.jakojäännös.roguelite.game.data.components.PlayerTag;
import fi.jakojäännös.roguelite.game.data.components.Position;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL30.*;

@Slf4j
public class PlayerRendererSystem implements ECSSystem<GameState>, AutoCloseable {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Position.class, PlayerTag.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    private final LWJGLCamera camera;
    private final int shaderProgram;
    private final int vertexShader;
    private final int fragmentShader;

    private float[] vertices;
    private float[] modelTransformationMatrix;
    private int vao;
    private int vbo;
    private int ebo;

    public PlayerRendererSystem(LWJGLCamera camera) {
        this.camera = camera;

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        val posX = 0.0f;
        val posY = 0.0f;
        val width = 1.0f;
        val height = 1.0f;
        this.vertices = new float[]{
                posX, posY,
                posX + width, posY,
                posX + width, posY + height,
                posX, posY + height,
        };
        this.vbo = glGenBuffers();
        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        val indices = new int[]{
                0, 1, 2,
                2, 3, 0,
        };
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        this.modelTransformationMatrix = new Matrix4f().identity().get(new float[4 * 4]);

        this.vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(this.vertexShader,
                       "#version 150\n" +
                               "\n" +
                               "in vec2 in_pos;\n" +
                               "\n" +
                               "uniform mat4 projection;\n" +
                               "uniform mat4 model;\n" +
                               "uniform mat4 view;\n" +
                               "\n" +
                               "void main(void) {\n" +
                               "   mat4 mvp = projection * view * model;\n" +
                               "   gl_Position = mvp * vec4(in_pos.x, in_pos.y, 0.0, 1.0);\n" +
                               "}\n"
        );
        glCompileShader(this.vertexShader);

        this.fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(this.fragmentShader,
                       "#version 150\n" +
                               "\n" +
                               "out vec4 out_fragColor;\n" +
                               "\n" +
                               "void main(void) {\n" +
                               "    out_fragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" +
                               "}\n"
        );
        glCompileShader(this.fragmentShader);

        if (glGetShaderi(this.vertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            LOG.error("Error compiling vertex shader:\n{}", glGetShaderInfoLog(this.vertexShader));
        }

        if (glGetShaderi(this.fragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
            LOG.error("Error compiling fragment shader:\n{}", glGetShaderInfoLog(this.fragmentShader));
        }

        this.shaderProgram = glCreateProgram();
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
    public void tick(
            Stream<Entity> entities,
            GameState state,
            double partialTickAlpha,
            Cluster cluster
    ) {
        glUseProgram(this.shaderProgram);

        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferData(GL_ARRAY_BUFFER, this.vertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 2, 0);

        val uniformModel = glGetUniformLocation(this.shaderProgram, "model");
        val uniformView = glGetUniformLocation(this.shaderProgram, "view");
        val uniformProj = glGetUniformLocation(this.shaderProgram, "projection");

        glUniformMatrix4fv(uniformView, false, this.camera.getViewMatrix());
        glUniformMatrix4fv(uniformProj, false, this.camera.getProjectionMatrix());

        val modelMatrix = new Matrix4f();
        entities.forEach(entity -> {
            state.world.getComponentOf(entity, Position.class)
                       .ifPresent(position -> {
                           modelMatrix.identity()
                                      .translate(position.x, position.y, 0.0f)
                                      .scale(state.playerSize)
                                      .get(this.modelTransformationMatrix);

                           glUniformMatrix4fv(uniformModel, false, this.modelTransformationMatrix);
                           glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
                       });
        });
    }

    @Override
    public void close() {
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteBuffers(this.ebo);

        glDeleteShader(this.vertexShader);
        glDeleteShader(this.fragmentShader);
        glDeleteProgram(this.shaderProgram);
    }
}
