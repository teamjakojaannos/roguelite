package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.ShaderProgram;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

@Slf4j
public class EntityCollisionBoundsRenderingSystem implements ECSSystem, AutoCloseable {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.tickAfter(SpriteRenderingSystem.class)
                    .withComponent(Transform.class)
                    .withComponent(Collider.class);
    }

    private final LWJGLCamera camera;
    private final ShaderProgram shader;
    private final int uniformProjectionMatrix;
    private final int uniformViewMatrix;
    private final int uniformModelMatrix;

    private int vao;
    private int vbo;
    private int ebo;

    private final Matrix4f modelMatrix = new Matrix4f();

    public EntityCollisionBoundsRenderingSystem(
            final Path assetRoot,
            final LWJGLCamera camera
    ) {
        this.camera = camera;
        this.shader = new ShaderProgram(
                assetRoot.resolve("shaders/bounds.vert"),
                assetRoot.resolve("shaders/bounds.frag"),
                Map.ofEntries(Map.entry(0, "in_pos")),
                Map.ofEntries(Map.entry(0, "out_fragColor"))
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
                0, 1,
                1, 2,
                2, 0,
                2, 3,
                3, 0,
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
            World world,
            double partialTickAlpha
    ) {
        this.shader.use();
        this.shader.setUniformMat4x4(this.uniformProjectionMatrix, this.camera.getProjectionMatrix());
        this.shader.setUniformMat4x4(this.uniformViewMatrix, this.camera.getViewMatrix());

        glBindVertexArray(this.vao);
        entities.forEach(
                entity -> {
                    if (world.getEntityManager().hasComponent(entity, NoDrawTag.class) || (!DebugConfig.renderBounds && world.getEntityManager().hasComponent(entity, SpriteInfo.class))) {
                        return;
                    }

                    Transform transform = world.getEntityManager().getComponentOf(entity, Transform.class).get();
                    Collider collider = world.getEntityManager().getComponentOf(entity, Collider.class).get();
                    this.shader.setUniformMat4x4(this.uniformModelMatrix,
                                                 modelMatrix.identity()
                                                            .translate((float) (transform.position.x - collider.origin.x),
                                                                       (float) (transform.position.y - collider.origin.y), 0.0f)
                                                            .scaleXY((float) collider.width, (float) collider.height)
                    );
                    glDrawElements(GL_LINES, 10, GL_UNSIGNED_INT, 0);
                }
        );
    }

    @Override
    public void close() {
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteBuffers(this.ebo);

        this.shader.close();
    }
}
