package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;

import lombok.Getter;
import lombok.val;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;

public class Font implements AutoCloseable {
    private final float contentScaleX, contentScaleY;

    private final ByteBuffer ttf;
    @Getter private final STBTTFontinfo fontInfo;

    private final Map<Integer, FontTexture> sizes = new HashMap<>();

    private final int ascent;
    private final int descent;
    private final int lineGap;

    public Font(
            final Path assetRoot,
            final float contentScaleX,
            final float contentScaleY
    ) {
        this.contentScaleX = contentScaleX;
        this.contentScaleY = contentScaleY;

        val path = assetRoot.resolve("fonts/VCR_OSD_MONO.ttf");
        try (SeekableByteChannel fc = Files.newByteChannel(path)) {
            this.ttf = BufferUtils.createByteBuffer((int) fc.size() + 1);
            //noinspection StatementWithEmptyBody
            while (fc.read(this.ttf) != -1) ;
            this.ttf.flip();
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Could not load font from %s!", path.toString()));
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
    }

    public FontTexture getTextureForSize(final int fontSize) {
        return this.sizes.computeIfAbsent(fontSize, key -> new FontTexture(this.ttf, this.fontInfo, key, this.contentScaleX, this.contentScaleY));
    }

    public float getLineOffset() {
        return this.ascent - this.descent + this.lineGap;
    }

    @Override
    public void close() {
        this.sizes.values().forEach(FontTexture::close);
    }
}
