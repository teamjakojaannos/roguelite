package fi.jakojaannos.roguelite.engine.view.rendering;

import fi.jakojaannos.roguelite.engine.view.Camera;
import org.joml.Matrix4f;

public interface SpriteBatch<TSpriteID, TCamera extends Camera> extends AutoCloseable {
    /**
     * Begins a new rendering batch. A batch must be finished using {@link #end()}, before calling
     * {@link #begin} again.
     *
     * @param camera camera to use as the rendering context
     */
    default void begin(TCamera camera) {
        begin(camera, null);
    }

    /**
     * Begins a new rendering batch. Applies provided global transformation. A batch must be
     * finished using {@link #end()}, before calling {@link #begin} again.
     *
     * @param camera         camera to use as the rendering context
     * @param transformation additional global transformation to apply. May be <code>null</code>
     */
    void begin(TCamera camera, Matrix4f transformation);

    /**
     * Renders the whole sprite texture at given coordinates.
     *
     * @param sprite identifier of the sprite to render
     * @param x      world x-coordinate where the sprite should be placed
     * @param y      world y-coordinate where the sprite should be placed
     */
    default void draw(TSpriteID sprite, double x, double y) {
        draw(sprite, -1, x, y);
    }

    /**
     * Renders the sprite at given coordinates. Only the frame indicated in the parameter
     * <code>frame</code> is rendered.
     *
     * @param sprite identifier of the sprite to render
     * @param frame  frame to render
     * @param x      world x-coordinate where the sprite should be placed
     * @param y      world y-coordinate where the sprite should be placed
     */
    default void draw(TSpriteID sprite, int frame, double x, double y) {
        draw(sprite, frame, x, y, 1.0, 1.0);
    }

    /**
     * Renders the sprite at given coordinates with given size. Only the frame indicated in the
     * parameter <code>frame</code> is rendered.
     *
     * @param sprite identifier of the sprite to render
     * @param frame  frame to render
     * @param x      world x-coordinate where the sprite should be placed
     * @param y      world y-coordinate where the sprite should be placed
     * @param width  horizontal size of the sprite in world units
     * @param height vertical size of the sprite in world units
     */
    default void draw(
            TSpriteID sprite,
            int frame,
            double x,
            double y,
            double width,
            double height
    ) {
        draw(sprite, frame, x, y, 0.0, 0.0, width, height, 0.0);
    }

    /**
     * Renders the sprite at given coordinates with given size. Only the frame indicated in the
     * parameter <code>frame</code> is rendered.
     *
     * @param sprite   identifier of the sprite to render
     * @param frame    frame to render
     * @param x        world x-coordinate where the sprite should be placed
     * @param y        world y-coordinate where the sprite should be placed
     * @param originX  origin offset on x-axis
     * @param originY  origin offset on y-axis
     * @param width    horizontal size of the sprite in world units
     * @param height   vertical size of the sprite in world units
     * @param rotation rotation, in radians, counter-clockwise
     */
    void draw(
            TSpriteID sprite,
            int frame,
            double x,
            double y,
            double originX,
            double originY,
            double width,
            double height,
            double rotation
    );

    void end();
}
