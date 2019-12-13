package fi.jakojaannos.roguelite.engine.view.sprite.serialization;

import com.google.gson.*;
import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.view.LogCategories;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import fi.jakojaannos.roguelite.engine.view.sprite.Animation;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SpriteDeserializer<TTexture extends Texture>
        implements JsonDeserializer<Sprite<TTexture>> {

    private final AssetRegistry<TTexture> textures;

    public SpriteDeserializer(AssetRegistry<TTexture> textures) {
        this.textures = textures;
    }

    @Override
    public Sprite<TTexture> deserialize(
            final JsonElement json,
            final Type typeOfT,
            final JsonDeserializationContext context
    ) throws JsonParseException {
        val jsonObject = json.getAsJsonObject();

        val frames = new ArrayList<TextureRegion<TTexture>>();
        deserializeFrames(jsonObject, frames);

        val animationsJson = jsonObject.getAsJsonObject("animations");
        val animations = new HashMap<String, Animation>();
        deserializeAnimations(animationsJson, frames.size(), animations);

        return new Sprite<>(List.copyOf(frames), Map.copyOf(animations));
    }

    private void deserializeFrames(
            final JsonObject root,
            final ArrayList<TextureRegion<TTexture>> outFrames
    ) {
        val framesElement = root.get("frames");
        if (framesElement.isJsonObject()) {
            // TODO: Serialize as atlas
            throw new UnsupportedOperationException("Not implemented");
        } else {
            val framesJsonArray = framesElement.getAsJsonArray();
            for (val frameElement : framesJsonArray) {
                deserializeSingleFrame(outFrames, frameElement);
            }
        }
    }

    private void deserializeSingleFrame(
            final ArrayList<TextureRegion<TTexture>> outFrames,
            final JsonElement frameElement
    ) {
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
        outFrames.add(new TextureRegion<>(texture, u0, v0, u1, v1));
    }

    private void deserializeAnimations(
            @Nullable final JsonObject animationsJson,
            final int frameCount,
            final Map<String, Animation> animations
    ) {
        if (animationsJson == null) {
            LOG.trace(LogCategories.SPRITE_SERIALIZATION,
                      "=> No animations found. Defaulting to infinite individual frames of all available frames.");
            animations.put("default", Animation.forFrameRange(0, frameCount, Double.POSITIVE_INFINITY));
            return;
        }

        for (val animationEntry : animationsJson.entrySet()) {
            deserializeAnimation(animations, animationEntry);
        }
    }

    private void deserializeAnimation(
            Map<String, Animation> animations,
            Map.Entry<String, JsonElement> animationEntry
    ) {
        val animationName = animationEntry.getKey();
        val animationElement = animationEntry.getValue();

        Animation animation;
        // JsonArray => list of frames
        if (animationElement.isJsonArray()) {
            val animationArray = animationElement.getAsJsonArray();
            List<Animation.Frame> animationFrames = new ArrayList<>();
            for (val frameElement : animationArray) {
                val frameObj = frameElement.getAsJsonObject();

                animationFrames.add(new Animation.Frame(frameObj.get("index").getAsInt(),
                                                        frameObj.get("duration").getAsDouble()));
            }

            animation = Animation.forFrames(animationFrames);
        }
        // Object => single frame or range
        else if (animationElement.isJsonObject()) {
            // TODO
            throw new UnsupportedOperationException("Not implemented");
        }
        // Something else? Error.
        else {
            throw new JsonParseException("Malformed animation frame definition!");
        }

        animations.put(animationName, animation);
    }
}
