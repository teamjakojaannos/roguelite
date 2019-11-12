package fi.jakojäännös.roguelite.game.view.systems;

import fi.jakojäännös.roguelite.engine.ecs.Cluster;
import fi.jakojäännös.roguelite.engine.ecs.Component;
import fi.jakojäännös.roguelite.engine.ecs.ECSSystem;
import fi.jakojäännös.roguelite.engine.ecs.Entity;
import fi.jakojäännös.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojäännös.roguelite.engine.lwjgl.view.rendering.ShaderProgram;
import fi.jakojäännös.roguelite.engine.utilities.io.TextFileHelper;
import fi.jakojäännös.roguelite.game.data.GameState;
import fi.jakojäännös.roguelite.game.data.components.PlayerTag;
import fi.jakojäännös.roguelite.game.data.components.Position;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;

import java.io.IOException;
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
    private final ShaderProgram shader;

    private float[] vertices;
    private float[] modelTransformationMatrix;
    private int vao;
    private int vbo;
    private int ebo;

    public PlayerRendererSystem(LWJGLCamera camera) {
        this.camera = camera;
        this.shader = new ShaderProgram(
                "assets/shaders/sprite.vert",
                "assets/shaders/sprite.frag"
        );

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
    }

    @Override
    public void tick(
            Stream<Entity> entities,
            GameState state,
            double partialTickAlpha,
            Cluster cluster
    ) {
        glUseProgram(this.shader.getShaderProgram());

        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferData(GL_ARRAY_BUFFER, this.vertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 2, 0);

        val uniformModel = glGetUniformLocation(this.shader.getShaderProgram(), "model");
        val uniformView = glGetUniformLocation(this.shader.getShaderProgram(), "view");
        val uniformProj = glGetUniformLocation(this.shader.getShaderProgram(), "projection");

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

        this.shader.close();
    }
}
