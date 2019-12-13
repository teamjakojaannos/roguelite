package fi.jakojaannos.roguelite.engine.view.rendering;

import fi.jakojaannos.roguelite.engine.view.Camera;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;

@Slf4j
public abstract class SpriteBatchBase<TSpriteID, TCamera extends Camera, TTexture extends Texture>
        implements SpriteBatch<TSpriteID, TCamera> {
    private final int maxFramesPerBatch;

    private boolean beginCalled;
    private TCamera activeCamera;
    private Matrix4f activeTransformation;
    private TTexture activeTexture;
    @Getter(AccessLevel.PROTECTED) private int nFrames;
    private int drawCalls;

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
            TTexture texture,
            TCamera camera,
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

    public abstract TextureRegion<TTexture> resolveTexture(TSpriteID sprite, int frame);

    /**
     * Queues a new sprite animation frame for rendering. Passing in -1 as the frame renders the
     * whole texture.
     *
     * @param texture texture to render
     * @param x       world x-coordinate to place the sprite to
     * @param y       world y-coordinate to place the sprite to
     * @param width   horizontal size of the sprite in world units
     * @param height  vertical size of the sprite in world units
     */
    protected abstract void queueFrame(
            TextureRegion<TTexture> texture,
            double x,
            double y,
            double width,
            double height
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
        if (this.nFrames > 0) {
            flush(this.activeTexture, this.activeCamera, this.activeTransformation);
            ++this.drawCalls;
        }
        this.nFrames = 0;
        this.activeTexture = null;
        this.activeCamera = null;
        this.activeTransformation = null;
        this.beginCalled = false;

        //LOG.debug("{} drawcalls", this.drawCalls);
        this.drawCalls = 0;
    }

    @Override
    public void draw(TSpriteID sprite, int frame, double x, double y, double width, double height) {
        val textureRegion = resolveTexture(sprite, frame);
        if (this.activeTexture == null) {
            this.activeTexture = textureRegion.getTexture();
        }

        val needToChangeTexture = !textureRegion.getTexture().equals(this.activeTexture);
        val batchIsFull = this.nFrames >= this.maxFramesPerBatch - 1;
        if (needToChangeTexture || batchIsFull) {
            flush(this.activeTexture, this.activeCamera, this.activeTransformation);
            this.nFrames = 0;
            this.activeTexture = textureRegion.getTexture();
            ++this.drawCalls;
        }

        queueFrame(textureRegion, x, y, width, height);
        this.nFrames += 1;
    }
}
