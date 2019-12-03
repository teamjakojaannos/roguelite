package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;

// TODO: Get rid of the BufferedImage to get rid of the java.desktop module read

@Slf4j
@EqualsAndHashCode
public class LWJGLTexture implements AutoCloseable {
    private final int texture;
    @Getter private final int width;
    @Getter private final int height;

    public LWJGLTexture(
            Path assetRoot,
            String path
    ) {
        this.texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        int width, height;
        try (val inputStream = Files.newInputStream(assetRoot.resolve(path));
             val stack = MemoryStack.stackPush()
        ) {
            val image = ImageIO.read(inputStream);
            width = image.getWidth();
            height = image.getHeight();
            val buffer = loadImageData(stack, image, width, height);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        } catch (IOException e) {
            LOG.warn("Image in path \"{}\" could not be opened!", assetRoot.resolve(path).toString());
            width = 0;
            height = 0;
        }
        this.width = width;
        this.height = height;

        LOG.debug("Done loading texture! {}×{}", width, height);
    }

    void bind() {
        glBindTexture(GL_TEXTURE_2D, this.texture);
    }

    @Override
    public void close() {
        glDeleteTextures(this.texture);
    }

    private ByteBuffer loadImageData(
            MemoryStack stack,
            BufferedImage image,
            int width,
            int height
    ) {
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        val buffer = stack.malloc(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));  // R
                buffer.put((byte) ((pixel >> 8) & 0xFF));   // G
                buffer.put((byte) (pixel & 0xFF));          // B
                buffer.put((byte) ((pixel >> 24) & 0xFF));  // A
            }
        }

        buffer.flip();

        return buffer;
    }

}
