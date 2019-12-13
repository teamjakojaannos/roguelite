package fi.jakojaannos.roguelite.engine.content.view;

import fi.jakojaannos.roguelite.engine.content.AbstractAssetRegistry;
import fi.jakojaannos.roguelite.engine.content.AssetHandle;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
public class TextureRegistry<TTexture extends Texture>
        extends AbstractAssetRegistry<TTexture> {
    private final Path assetRoot;
    private final TextureLoader<TTexture> textureLoader;
    private final TTexture defaultTexture;

    public TextureRegistry(final Path assetRoot, TextureLoader<TTexture> textureLoader) {
        this.assetRoot = assetRoot;
        this.textureLoader = textureLoader;
        this.defaultTexture = getByAssetName("textures/sheep.png");
    }

    @Override
    protected TTexture getDefault() {
        return this.defaultTexture;
    }

    @Override
    protected Optional<TTexture> loadAsset(AssetHandle handle) {
        val path = assetRoot.resolve(handle.getName());
        try (val inputStream = Files.newInputStream(path)) {
            val image = ImageIO.read(inputStream);
            val width = image.getWidth();
            val height = image.getHeight();
            return Optional.of(this.textureLoader.load(width, height, image));
        } catch (IOException e) {
            LOG.warn("Image in path \"{}\" could not be opened!", path.toString());
            return Optional.empty();
        }
    }

    @Override
    public void close() throws Exception {
        forEach((handle, texture) -> {
            try {
                texture.close();
            } catch (Exception ignored) {
            }
        });
        super.close();
    }

    public interface TextureLoader<TTexture extends Texture> {
        TTexture load(int width, int height, BufferedImage image);
    }
}
