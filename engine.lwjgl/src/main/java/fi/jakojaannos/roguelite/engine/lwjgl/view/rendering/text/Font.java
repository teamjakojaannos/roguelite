package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;

import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;

import java.util.Map;

public class Font<TTexture extends Texture> {
    private final Map<String, TextureRegion<TTexture>> availableCharacters;

    public Font(final Map<String, TextureRegion<TTexture>> characters) {
        this.availableCharacters = Map.copyOf(characters);
    }
}
