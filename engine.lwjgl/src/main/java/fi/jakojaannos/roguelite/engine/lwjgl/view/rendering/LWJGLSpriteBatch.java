package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering;

import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatchBase;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
public class LWJGLSpriteBatch extends SpriteBatchBase<String, LWJGLCamera, LWJGLTexture> {
    private static final Matrix4f DEFAULT_TRANSFORM = new Matrix4f().identity();
    private static final int MAX_SPRITES_PER_BATCH = 4096;
    private static final int VERTICES_PER_SPRITE = 4;
    private static final int SIZE_IN_BYTES = (2 + 2 + 3) * 4;

    private final ShaderProgram shader;
    private final int uniformProjectionMatrix;
    private final int uniformViewMatrix;
    private final int uniformModelMatrix;

    private final int vao;
    private final int vbo;
    private final int ebo;

    private final ByteBuffer vertexDataBuffer;
    private final SpriteRegistry<LWJGLTexture> sprites;

    public LWJGLSpriteBatch(
            final Path assetRoot,
            final String shader,
            final SpriteRegistry<LWJGLTexture> sprites
    ) {
        super(MAX_SPRITES_PER_BATCH);
        this.sprites = sprites;
        this.shader = createShader(assetRoot, shader);
        this.uniformModelMatrix = this.shader.getUniformLocation("model");
        this.uniformViewMatrix = this.shader.getUniformLocation("view");
        this.uniformProjectionMatrix = this.shader.getUniformLocation("projection");

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        this.vbo = glGenBuffers();
        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        val indices = constructIndicesArray();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        this.vertexDataBuffer = MemoryUtil.memAlloc(MAX_SPRITES_PER_BATCH * VERTICES_PER_SPRITE * SIZE_IN_BYTES);
        for (int i = 0; i < MAX_SPRITES_PER_BATCH * VERTICES_PER_SPRITE; ++i) {
            updateVertex(i * SIZE_IN_BYTES, 0, 0, 0, 0, 0, 0, 0);
        }

        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferData(GL_ARRAY_BUFFER, this.vertexDataBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0,
                              2,
                              GL_FLOAT,
                              false,
                              SIZE_IN_BYTES,
                              0); // offset: pos is first attribute -> 0
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1,
                              2,
                              GL_FLOAT,
                              false,
                              SIZE_IN_BYTES,
                              2 * 4); // offset: pos = 2 * sizeof(float)
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2,
                              3,
                              GL_FLOAT,
                              false,
                              SIZE_IN_BYTES,
                              4 * 4); // offset: pos + uv = 4 * sizeof(float)
    }

    @Override
    public TextureRegion<LWJGLTexture> resolveTexture(final String spriteName, int frame) {
        return this.sprites.getByAssetName(spriteName)
                           .getSpecificFrame("default", frame);
    }

    @Override
    protected void queueFrame(
            final TextureRegion<LWJGLTexture> region,
            final double x,
            final double y,
            final double width,
            final double height
    ) {
        val offset = getNFrames() * VERTICES_PER_SPRITE * SIZE_IN_BYTES;
        updateVertex(offset,
                     x, y,
                     (float) region.getU0(), (float) region.getV0(),
                     1.0f, 1.0f, 1.0f);
        updateVertex(offset + SIZE_IN_BYTES,
                     x + width, y,
                     (float) region.getU1(), (float) region.getV0(),
                     1.0f, 1.0f, 1.0f);
        updateVertex(offset + (2 * SIZE_IN_BYTES),
                     x + width, y + height,
                     (float) region.getU1(), (float) region.getV1(),
                     1.0f, 1.0f, 1.0f);
        updateVertex(offset + (3 * SIZE_IN_BYTES),
                     x, y + height,
                     (float) region.getU0(), (float) region.getV1(),
                     1.0f, 1.0f, 1.0f);
    }

    private void updateVertex(
            int offset,
            double x,
            double y,
            double u,
            double v,
            float r,
            float g,
            float b
    ) {
        this.vertexDataBuffer.putFloat(offset, (float) x);
        this.vertexDataBuffer.putFloat(offset + 4, (float) y);
        this.vertexDataBuffer.putFloat(offset + 8, (float) u);
        this.vertexDataBuffer.putFloat(offset + 12, (float) v);
        this.vertexDataBuffer.putFloat(offset + 16, r);
        this.vertexDataBuffer.putFloat(offset + 20, g);
        this.vertexDataBuffer.putFloat(offset + 24, b);
    }

    @Override
    protected void flush(
            LWJGLTexture texture,
            LWJGLCamera camera,
            Matrix4f transformation
    ) {
        this.shader.use();
        texture.bind();

        this.shader.setUniformMat4x4(this.uniformProjectionMatrix, camera.getProjectionMatrix());
        this.shader.setUniformMat4x4(this.uniformViewMatrix, camera.getViewMatrix());
        this.shader.setUniformMat4x4(this.uniformModelMatrix,
                                     transformation != null
                                             ? transformation
                                             : DEFAULT_TRANSFORM);

        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        this.vertexDataBuffer.limit(getNFrames() * VERTICES_PER_SPRITE * SIZE_IN_BYTES);
        glBufferSubData(GL_ARRAY_BUFFER, 0, this.vertexDataBuffer);

        glDrawElements(GL_TRIANGLES,
                       getNFrames() * 6,
                       GL_UNSIGNED_INT,
                       NULL);

        this.vertexDataBuffer.limit(this.vertexDataBuffer.capacity());
    }

    @Override
    public void close() {
        MemoryUtil.memFree(this.vertexDataBuffer);
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteBuffers(this.ebo);

        this.shader.close();
    }

    private int[] constructIndicesArray() {
        val indices = new int[MAX_SPRITES_PER_BATCH * 6];
        for (int i = 0, j = 0; i < indices.length; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = j + 1;
            indices[i + 2] = j + 2;
            indices[i + 3] = j + 2;
            indices[i + 4] = j + 3;
            indices[i + 5] = j;
        }
        return indices;
    }

    private static ShaderProgram createShader(final Path assetRoot, final String shader) {
        return ShaderProgram.builder()
                            .vertexShader(assetRoot.resolve("shaders").resolve(shader + ".vert"))
                            .fragmentShader(assetRoot.resolve("shaders").resolve(shader + ".frag"))
                            .attributeLocation(0, "in_pos")
                            .attributeLocation(1, "in_uv")
                            .attributeLocation(2, "in_tint")
                            .fragmentDataLocation(0, "out_fragColor")
                            .build();
    }
}
