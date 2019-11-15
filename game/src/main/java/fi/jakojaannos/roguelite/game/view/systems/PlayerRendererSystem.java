package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.ShaderProgram;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.Position;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL20.*;
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
    private final int uniformProjectionMatrix;
    private final int uniformViewMatrix;
    private final int uniformModelMatrix;

    private int vao;
    private int vbo;
    private int ebo;

    public PlayerRendererSystem(@NonNull String assetRoot, @NonNull LWJGLCamera camera) {
        this.camera = camera;
        this.shader = new ShaderProgram(
                assetRoot + "shaders/sprite.vert",
                assetRoot + "shaders/sprite.frag"
        );
        this.uniformModelMatrix = this.shader.getUniformLocation("model");
        this.uniformViewMatrix = this.shader.getUniformLocation("view");
        this.uniformProjectionMatrix = this.shader.getUniformLocation("projection");

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        val posX = 0.0f;
        val posY = 0.0f;
        val width = 1.0f;
        val height = 1.0f;
        val vertices = new float[]{
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

        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 2, 0);
    }

    @Override
    public void tick(
            Stream<Entity> entities,
            GameState state,
            double partialTickAlpha,
            Cluster cluster
    ) {
        this.shader.use();
        this.shader.setUniformMat4x4(this.uniformProjectionMatrix, this.camera.getProjectionMatrix());
        this.shader.setUniformMat4x4(this.uniformViewMatrix, this.camera.getViewMatrix());

        val modelMatrix = new Matrix4f();
        val modelMatrixArray = new float[4 * 4];
        entities.forEach(
                entity -> state.world.getComponentOf(entity, Position.class)
                                     .ifPresent(position -> {
                                         modelMatrix.identity()
                                                    .translate((float) position.x, (float) position.y, 0.0f)
                                                    .scale(state.playerSize)
                                                    .get(modelMatrixArray);

                                         this.shader.setUniformMat4x4(this.uniformModelMatrix, modelMatrixArray);
                                         glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
                                     }));
    }

    @Override
    public void close() {
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteBuffers(this.ebo);

        this.shader.close();
    }
}
