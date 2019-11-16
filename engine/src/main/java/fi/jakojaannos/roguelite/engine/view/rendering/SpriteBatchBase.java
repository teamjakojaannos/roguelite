package fi.jakojaannos.roguelite.engine.view.rendering;

import fi.jakojaannos.roguelite.engine.view.Camera;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;

@Slf4j
public abstract class SpriteBatchBase<TSpriteID, TCamera extends Camera, TTexture>
        implements SpriteBatch<TSpriteID, TCamera> {
    private final int maxFramesPerBatch;

    private boolean beginCalled;
    private TCamera activeCamera;
    private Matrix4f activeTransformation;
    private TTexture activeTexture;
    @Getter(AccessLevel.PROTECTED) private int nFrames;

    protected SpriteBatchBase(int maxFramesPerBatch) {
        this.maxFramesPerBatch = maxFramesPerBatch;
    }

    /**
     * Flushes the currently queued sprites.
     *
     * @param texture        texture to use
     * @param camera         current rendering context
     * @param transformation global transformations to apply
     */
    protected abstract void flush(
            @NonNull TTexture texture,
            @NonNull TCamera camera,
            Matrix4f transformation
    );

    /**
     * Resolves the given sprite identifier into a texture.
     *
     * @param sprite identifier of the sprite to resolve
     * @param frame  current frame for the sprite to resolve
     *
     * @return texture which can be used to render the sprite
     */
    @NonNull
    public abstract TTexture resolveTexture(@NonNull TSpriteID sprite, int frame);

    /**
     * Queues a new sprite animation frame for rendering. Passing in -1 as the frame renders the
     * whole texture.
     *
     * @param texture texture to render
     * @param frame   frame to render. -1 means whole texture
     * @param x       world x-coordinate to place the sprite to
     * @param y       world y-coordinate to place the sprite to
     */
    protected abstract void queueFrame(
            @NonNull TTexture texture,
            int frame,
            double x,
            double y
    );

    @Override
    public void begin(TCamera camera, Matrix4f transformation) {
        this.activeCamera = camera;
        this.activeTransformation = transformation;
        if (this.beginCalled) {
            LOG.error("SpriteBatch.begin() called without calling .end() first!");
            return;
        }

        this.beginCalled = true;
    }

    @Override
    public void end() {
        if (!this.beginCalled) {
            LOG.error("SpriteBatch.end() called without calling .begin() first!");
            return;
        }

        flush(this.activeTexture, this.activeCamera, this.activeTransformation);
        this.nFrames = 0;
        this.activeTexture = null;
        this.activeCamera = null;
        this.activeTransformation = null;
        this.beginCalled = false;
    }

    @Override
    public void draw(TSpriteID sprite, int frame, double x, double y) {
        val texture = resolveTexture(sprite, frame);
        if (this.activeTexture == null) {
            this.activeTexture = texture;
        }

        if (!texture.equals(this.activeTexture) || this.nFrames == maxFramesPerBatch) {
            flush(this.activeTexture, this.activeCamera, this.activeTransformation);
            this.nFrames = 0;
            this.activeTexture = texture;
        }

        queueFrame(this.activeTexture, frame, x, y);
        this.nFrames += 1;
    }
}
