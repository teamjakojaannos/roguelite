package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;

import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.ShaderProgram;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
public class TextRenderer<TTexture extends Texture> implements AutoCloseable {
    private static final int SIZE_IN_BYTES = (2 + 2 + 3) * 4;

    private final STBTTBakedChar.Buffer bakedCharacters;
    private final ByteBuffer ttf;
    private final STBTTFontinfo fontInfo;
    private final int fontHeight;

    private final int ascent;
    private final int descent;
    private final int lineGap;
    private final int scaledBitmapW, scaledBitmapH;
    private final float contentScaleX, contentScaleY;
    private final boolean kerningEnabled = false;

    private final ShaderProgram shader;
    private final int uniformModelMatrix;
    private final int uniformViewMatrix;
    private final int uniformProjectionMatrix;

    private final ByteBuffer vertexDataBuffer;
    private final int vao;
    private final int vbo;
    private final int ebo;
    private final int textureId;
    private final LWJGLCamera camera;

    public TextRenderer(
            final Path assetRoot,
            final LWJGLCamera camera
    ) {
        this.camera = camera;

        val path = assetRoot.resolve("fonts/VCR_OSD_MONO.ttf");
        try (SeekableByteChannel fc = Files.newByteChannel(path)) {
            this.ttf = BufferUtils.createByteBuffer((int) fc.size() + 1);
            //noinspection StatementWithEmptyBody
            while (fc.read(this.ttf) != -1) ;
            this.ttf.flip();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load font!");
        }

        this.fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(this.fontInfo, this.ttf)) {
            throw new IllegalStateException("Failed to initialize font descriptor.");
        }

        try (val stack = MemoryStack.stackPush()) {
            val pAscent = stack.mallocInt(1);
            val pDescent = stack.mallocInt(1);
            val pLineGap = stack.mallocInt(1);

            stbtt_GetFontVMetrics(this.fontInfo, pAscent, pDescent, pLineGap);
            this.ascent = pAscent.get(0);
            this.descent = pDescent.get(0);
            this.lineGap = pLineGap.get(0);
        }

        this.textureId = glGenTextures();

        this.shader = createShader(assetRoot);
        this.uniformModelMatrix = this.shader.getUniformLocation("model");
        this.uniformViewMatrix = this.shader.getUniformLocation("view");
        this.uniformProjectionMatrix = this.shader.getUniformLocation("projection");

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        this.vbo = glGenBuffers();
        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        val indices = new int[]{
                0, 1, 2,
                3, 0, 2,
        };
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        this.vertexDataBuffer = MemoryUtil.memAlloc(4 * SIZE_IN_BYTES);

        this.contentScaleX = 4f;
        this.contentScaleY = 4f;
        this.scaledBitmapW = Math.round(512 * this.contentScaleX);
        this.scaledBitmapH = Math.round(512 * this.contentScaleY);
        this.fontHeight = 24;
        this.bakedCharacters = bakeFontToBitmap();

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private STBTTBakedChar.Buffer bakeFontToBitmap() {
        val cdata = STBTTBakedChar.malloc(96); // 96 ???
        val bitmap = BufferUtils.createByteBuffer(this.scaledBitmapW * this.scaledBitmapH);
        glBindTexture(GL_TEXTURE_2D, this.textureId);
        stbtt_BakeFontBitmap(this.ttf,
                             this.fontHeight * this.contentScaleY,
                             bitmap,
                             this.scaledBitmapW,
                             this.scaledBitmapH,
                             32,
                             cdata);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, this.scaledBitmapW, this.scaledBitmapH, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);

        return cdata;
    }

    public void drawOnScreen(
            final double x,
            final double y,
            final double height,
            final String string
    ) {
        this.shader.use();
        this.shader.setUniformMat4x4(this.uniformProjectionMatrix, new Matrix4f()
                .ortho(0,
                       this.camera.getViewportWidthInPixels(),
                       this.camera.getViewportHeightInPixels(),
                       0,
                       0,
                       100));
        this.shader.setUniformMat4x4(this.uniformViewMatrix, new Matrix4f().identity());
        this.shader.setUniformMat4x4(this.uniformModelMatrix, new Matrix4f().identity());
        draw(x, y, (float) height, string, 1, 1);
    }

    public void drawInWorld(
            final double x,
            final double y,
            final double height,
            final String string
    ) {
        this.shader.use();
        this.shader.setUniformMat4x4(this.uniformProjectionMatrix, this.camera.getProjectionMatrix());
        this.shader.setUniformMat4x4(this.uniformViewMatrix, this.camera.getViewMatrix());
        this.shader.setUniformMat4x4(this.uniformModelMatrix, new Matrix4f().identity());

        val pixelsPerUnitVertical = this.camera.getViewportHeightInPixels() / this.camera.getViewportHeightInUnits();
        val pixelsPerUnitHorizontal = this.camera.getViewportWidthInPixels() / this.camera.getViewportWidthInUnits();
        draw(x, y, (float) (pixelsPerUnitVertical / height), string, pixelsPerUnitVertical, pixelsPerUnitHorizontal);
    }

    private void draw(
            final double x,
            final double y,
            final float height,
            final String string,
            final double pixelsPerUnitVertical,
            final double pixelsPerUnitHorizontal
    ) {
        val scale = stbtt_ScaleForPixelHeight(this.fontInfo, height);

        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBindTexture(GL_TEXTURE_2D, this.textureId);
        try (val stack = MemoryStack.stackPush()) {
            val pCodePoint = stack.mallocInt(1);

            val pX = stack.floats(0.0f);
            val pY = stack.floats(0.0f);

            val alignedQuad = STBTTAlignedQuad.mallocStack(stack);

            val factorX = height / this.contentScaleX;
            val factorY = height / this.contentScaleY;

            var lineStart = 0;
            var lineY = 0.0f;

            for (int i = 0, to = string.length(); i < to; ) {
                i += getCP(string, to, i, pCodePoint);

                val cp = pCodePoint.get(0);
                if (cp == '\n') {
                    pX.put(0, 0.0f);
                    pY.put(0, lineY = pY.get(0) + (this.ascent - this.descent + this.lineGap) * scale);

                    lineStart = i;
                    continue;
                } else if (cp < 32 || cp >= 128) {
                    continue;
                }

                val cpX = pX.get(0);
                stbtt_GetBakedQuad(this.bakedCharacters,
                                   this.scaledBitmapW,
                                   this.scaledBitmapH,
                                   cp - 32,
                                   pX,
                                   pY,
                                   alignedQuad,
                                   true);
                pX.put(0, (float) scale(cpX, pX.get(0), factorX / pixelsPerUnitHorizontal));
                if (this.kerningEnabled && i < to) {
                    getCP(string, to, i, pCodePoint);
                    pX.put(0, pX.get(0) + stbtt_GetCodepointKernAdvance(this.fontInfo, cp, pCodePoint.get(0)) * scale);
                }

                val x0 = x + scale(cpX, alignedQuad.x0(), factorX / pixelsPerUnitHorizontal);
                val x1 = x + scale(cpX, alignedQuad.x1(), factorX / pixelsPerUnitHorizontal);
                val y0 = y + scale(lineY, alignedQuad.y0(), factorY / pixelsPerUnitVertical);
                val y1 = y + scale(lineY, alignedQuad.y1(), factorY / pixelsPerUnitVertical);
                val u0 = alignedQuad.s0();
                val u1 = alignedQuad.s1();
                val v0 = alignedQuad.t0();
                val v1 = alignedQuad.t1();

                updateVertex(0, x0, y0, u0, v0, 1.0f, 1.0f, 1.0f);
                updateVertex(SIZE_IN_BYTES, x1, y0, u1, v0, 1.0f, 1.0f, 1.0f);
                updateVertex(2 * SIZE_IN_BYTES, x1, y1, u1, v1, 1.0f, 1.0f, 1.0f);
                updateVertex(3 * SIZE_IN_BYTES, x0, y1, u0, v1, 1.0f, 1.0f, 1.0f);


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

                glDrawElements(GL_TRIANGLES,
                               6,
                               GL_UNSIGNED_INT,
                               NULL);
            }
        }
    }

    private void updateVertex(
            final int offset,
            final double x,
            final double y,
            final double u,
            final double v,
            final float r,
            final float g,
            final float b
    ) {
        this.vertexDataBuffer.putFloat(offset, (float) x);
        this.vertexDataBuffer.putFloat(offset + 4, (float) y);
        this.vertexDataBuffer.putFloat(offset + 8, (float) u);
        this.vertexDataBuffer.putFloat(offset + 12, (float) v);
        this.vertexDataBuffer.putFloat(offset + 16, r);
        this.vertexDataBuffer.putFloat(offset + 20, g);
        this.vertexDataBuffer.putFloat(offset + 24, b);
    }

    private static int getCP(
            final String string,
            final int to,
            final int i,
            final IntBuffer outCodePoint
    ) {
        val charA = string.charAt(i);
        if (Character.isHighSurrogate(charA) && i + 1 < to) {
            val charB = string.charAt(i + 1);
            if (Character.isLowSurrogate(charB)) {
                outCodePoint.put(0, Character.toCodePoint(charA, charB));
                return 2;
            }
        }

        outCodePoint.put(0, charA);
        return 1;
    }

    private double scale(
            final double center,
            final double offset,
            final double factor
    ) {
        return (offset - center) * factor + center;
    }

    private static ShaderProgram createShader(final Path assetRoot) {
        return ShaderProgram.builder()
                            .vertexShader(assetRoot.resolve("shaders").resolve("text.vert"))
                            .fragmentShader(assetRoot.resolve("shaders").resolve("text.frag"))
                            .attributeLocation(0, "in_pos")
                            .attributeLocation(1, "in_uv")
                            .attributeLocation(2, "in_tint")
                            .fragmentDataLocation(0, "out_fragColor")
                            .build();
    }

    @Override
    public void close() {
        this.bakedCharacters.close();

        MemoryUtil.memFree(this.vertexDataBuffer);
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteBuffers(this.ebo);

        this.shader.close();
    }
}
