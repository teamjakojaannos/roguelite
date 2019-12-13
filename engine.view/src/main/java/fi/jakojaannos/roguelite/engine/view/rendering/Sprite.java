package fi.jakojaannos.roguelite.engine.view.rendering;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Sprite<TTexture extends Texture> {
    private final List<TextureRegion<TTexture>> frames;

    public Sprite(List<TextureRegion<TTexture>> frames) {
        this.frames = frames;
    }

    public TextureRegion<TTexture> getFrame(final int frame) {
        var wrappedFrame = frame;
        if (frame < 0 || frame >= this.frames.size()) {
            LOG.warn("Tried to get a frame from a sprite with an invalid frame index \"{}\"!", frame);
            wrappedFrame %= this.frames.size();
        }
        return this.frames.get(wrappedFrame);
    }

    public TextureRegion<TTexture> getFrameOrWhole(final int frame) {
        return frame == -1 && this.frames.size() == 1
                ? this.frames.get(0)
                : this.getFrame(frame);
    }
}
