package fi.jakojäännös.roguelite.engine.lwjgl.view;

import fi.jakojäännös.roguelite.engine.view.Camera;
import fi.jakojäännös.roguelite.game.data.GameState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL11.glViewport;

@Slf4j
public class LWJGLCamera extends Camera {
    private static final double CAMERA_MOVE_EPSILON = 0.0001;

    private float targetScreenSizeInUnits;

    @Getter private float viewportWidthInUnits;
    @Getter private float viewportHeightInUnits;

    private int viewportWidth;
    private int viewportHeight;

    private final Matrix4f projectionMatrix;
    private final float[] cachedProjectionMatrixArray;
    private boolean projectionMatrixDirty;

    private final Matrix4f viewMatrix;
    private final float[] cachedViewMatrixArray;
    private boolean viewMatrixDirty;

    // TODO: Wrap all rendering requiring arrays to use some immutable utility class. This is unsafe.
    public float[] getViewMatrix() {
        refreshViewMatrixIfDirty();
        return cachedViewMatrixArray;
    }

    public float[] getProjectionMatrix() {
        refreshProjectionMatrixIfDirty();
        return cachedProjectionMatrixArray;
    }

    public void resizeViewport(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        glViewport(0, 0, this.viewportWidth, this.viewportHeight);

        this.projectionMatrixDirty = true;
        LOG.info("Resizing viewport: {}x{}", this.viewportWidth, this.viewportHeight);
    }

    @Override
    public void setPosition(float x, float y) {
        double dx = x - getX();
        double dy = y - getY();
        if (dx * dx + dy * dy > CAMERA_MOVE_EPSILON || this.viewMatrixDirty) {
            super.setPosition(x, y);
            this.viewMatrixDirty = true;
        }
    }

    public LWJGLCamera() {
        super(new Vector2f(0f, 0.0f));

        this.targetScreenSizeInUnits = 32;

        this.projectionMatrix = new Matrix4f().identity();
        this.cachedProjectionMatrixArray = new float[16];
        this.projectionMatrixDirty = true;
        resizeViewport(viewportWidth, viewportHeight);

        this.viewMatrix = new Matrix4f();
        this.cachedViewMatrixArray = new float[16];
        this.viewMatrixDirty = true;
        refreshViewMatrixIfDirty();
    }

    public void updateConfigurationFromState(GameState state) {
        if (state.targetWorldSize != this.targetScreenSizeInUnits) {
            this.targetScreenSizeInUnits = state.targetWorldSize;
            this.projectionMatrixDirty = true;
        }

        // FIXME: THIS BREAKS MVC ENCAPSULATION. Technically, we should queue task on the controller
        //  to make the change, NEVER mutate state on the view.
        state.realViewWidth = this.viewportWidthInUnits;
        state.realViewHeight = this.viewportHeightInUnits;
    }

    private void refreshProjectionMatrixIfDirty() {
        if (this.projectionMatrixDirty) {
            LOG.trace("Refreshing projection matrix");

            val horizontalMajor = this.viewportWidth > this.viewportHeight;
            double major = horizontalMajor ? this.viewportWidth : this.viewportHeight;
            double minor = horizontalMajor ? this.viewportHeight : this.viewportWidth;

            // TODO: Find such realTargetSize that pixelsPerUnit is a positive whole number to avoid
            //  aliasing.
            double realTargetSize = this.targetScreenSizeInUnits;
            val pixelsPerUnit = horizontalMajor
                    ? this.viewportWidth / realTargetSize
                    : this.viewportHeight / realTargetSize;

            val ratio = minor / major;
            this.viewportWidthInUnits = (float) (horizontalMajor
                    ? realTargetSize
                    : ratio * realTargetSize);
            this.viewportHeightInUnits = (float) (horizontalMajor
                    ? ratio * realTargetSize
                    : realTargetSize);
            this.projectionMatrix.setOrtho2D(
                    0.0f,
                    (float) viewportWidthInUnits,
                    (float) viewportHeightInUnits,
                    0.0f);
            this.projectionMatrix.get(this.cachedProjectionMatrixArray);

            this.projectionMatrixDirty = false;
        }
    }

    private void refreshViewMatrixIfDirty() {
        if (this.viewMatrixDirty) {
            LOG.trace("Refreshing view matrix");
            this.viewMatrix
                    .identity()
                    .translate(getX(), getY(), 0.0f)
                    //.scale(this.zoom);
                    .invert();
            this.viewMatrix.get(this.cachedViewMatrixArray);

            this.viewMatrixDirty = false;
        }
    }
}
