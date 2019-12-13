package fi.jakojaannos.roguelite.engine.content.view;

import com.google.gson.*;
import fi.jakojaannos.roguelite.engine.content.AbstractAssetRegistry;
import fi.jakojaannos.roguelite.engine.content.AssetHandle;
import fi.jakojaannos.roguelite.engine.view.rendering.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles loading sprites from assets-directory.
 */
@Slf4j
public class SpriteRegistry<TTexture extends Texture>
        extends AbstractAssetRegistry<Sprite<TTexture>> {
    private final Path assetRoot;

    private final TextureRegistry<TTexture> textures;
    private final Sprite<TTexture> defaultSprite;

    public SpriteRegistry(
            final Path assetRoot,
            final TextureRegistry<TTexture> textures
    ) {
        this.assetRoot = assetRoot;
        this.textures = textures;

        this.defaultSprite = new Sprite<>(List.of(new TextureRegion<>(textures.getDefault(),
                                                                      0, 0,
                                                                      1, 1)));
    }

    @Override
    protected Sprite<TTexture> getDefault() {
        return this.defaultSprite;
    }

    @Override
    protected Optional<Sprite<TTexture>> loadAsset(final AssetHandle handle) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Sprite.class, (JsonDeserializer<Sprite<TTexture>>) this::deserializeSprite)
                .create();
        try (val reader = new InputStreamReader(Files.newInputStream(assetRoot.resolve(handle.getName() + ".json"), StandardOpenOption.READ))) {
            // noinspection unchecked
            return Optional.ofNullable(gson.fromJson(reader, Sprite.class));
        } catch (IOException e) {
            LOG.error("Reading sprite \"{}\" failed!", handle);
            LOG.error("Exception: ", e);
            return Optional.empty();
        }
    }

    private Sprite<TTexture> deserializeSprite(
            final JsonElement json,
            final Type typeOfT,
            final JsonDeserializationContext context
    ) throws JsonParseException {
        val jsonObject = json.getAsJsonObject();
        val framesJson = jsonObject.getAsJsonArray("frames");

        val frames = new ArrayList<TextureRegion<TTexture>>();
        for (val frameElement : framesJson) {
            val frameJson = frameElement.getAsJsonObject();
            val textureHandle = frameJson.get("texture").getAsString();
            val texture = this.textures.getByAssetName(textureHandle);
            val x = frameJson.get("x").getAsDouble();
            val y = frameJson.get("y").getAsDouble();
            val w = frameJson.get("w").getAsDouble();
            val h = frameJson.get("h").getAsDouble();
            val u0 = x / texture.getWidth();
            val v0 = y / texture.getHeight();
            val u1 = u0 + w / texture.getWidth();
            val v1 = v0 + h / texture.getHeight();
            frames.add(new TextureRegion<>(texture, u0, v0, u1, v1));
        }

        return new Sprite<>(frames);
    }

    @Override
    public void close() {
    }
}
