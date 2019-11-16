package fi.jakojaannos.roguelite.engine.lwjgl.view;

import fi.jakojaannos.roguelite.engine.view.Camera;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL11.glViewport;

@Slf4j
public class LWJGLCamera extends Camera {
    private static final double CAMERA_MOVE_EPSILON = 0.0001;

    @Getter(AccessLevel.PROTECTED)
    private double targetScreenSizeInUnits = 32.0;

    @Getter private float viewportWidthInUnits;
    @Getter private float viewportHeightInUnits;

    private int viewportWidth;
    private int viewportHeight;

    private final Matrix4f projectionMatrix;
    private boolean projectionMatrixDirty;

    private final Matrix4f viewMatrix;
    private boolean viewMatrixDirty;

    public Matrix4f getViewMatrix() {
        refreshViewMatrixIfDirty();
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        refreshProjectionMatrixIfDirty();
        return projectionMatrix;
    }

    protected void setTargetScreenSizeInUnits(double targetSize) {
        this.targetScreenSizeInUnits = targetSize;
        this.projectionMatrixDirty = true;
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

    public LWJGLCamera(int viewportWidth, int viewportHeight) {
        super(new Vector2f(0f, 0.0f));
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;

        this.projectionMatrix = new Matrix4f().identity();
        this.projectionMatrixDirty = true;
        resizeViewport(viewportWidth, viewportHeight);

        this.viewMatrix = new Matrix4f();
        this.viewMatrixDirty = true;
        refreshViewMatrixIfDirty();
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

            val ratio = major / minor;
            this.viewportWidthInUnits = (float) (horizontalMajor
                    ? ratio * realTargetSize
                    : realTargetSize);
            this.viewportHeightInUnits = (float) (horizontalMajor
                    ? realTargetSize
                    : ratio * realTargetSize);
            this.projectionMatrix.setOrtho2D(
                    0.0f,
                    (float) viewportWidthInUnits,
                    (float) viewportHeightInUnits,
                    0.0f);

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

            this.viewMatrixDirty = false;
        }
    }
}
