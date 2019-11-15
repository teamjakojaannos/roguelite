package fi.jakojaannos.roguelite.game.view;

import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.game.data.GameState;

class RogueliteCamera extends LWJGLCamera {
    public RogueliteCamera(int viewportWidth, int viewportHeight) {
        super(viewportWidth, viewportHeight);
    }

    void updateConfigurationFromState(GameState state) {
        if (state.targetWorldSize != getTargetScreenSizeInUnits()) {
            setTargetScreenSizeInUnits(state.targetWorldSize);
        }

        // FIXME: THIS BREAKS MVC ENCAPSULATION. Technically, we should queue task on the controller
        //  to make the change, NEVER mutate state on the view.
        state.realViewWidth = getViewportWidthInUnits();
        state.realViewHeight = getViewportHeightInUnits();
    }
}
