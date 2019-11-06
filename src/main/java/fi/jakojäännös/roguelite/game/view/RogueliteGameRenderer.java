package fi.jakojäännös.roguelite.game.view;

import fi.jakojäännös.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojäännös.roguelite.engine.lwjgl.view.LWJGLWindow;
import fi.jakojäännös.roguelite.engine.view.GameRenderer;
import fi.jakojäännös.roguelite.game.Roguelite;

public class RogueliteGameRenderer implements GameRenderer<Roguelite> {
    private final LWJGLCamera camera;

    public RogueliteGameRenderer(LWJGLWindow window) {
        this.camera = new LWJGLCamera();

        window.setResizeCallback(this.camera::resizeViewport);
    }

    @Override
    public void render(Roguelite game, double delta) {
        // 1. Find entity tagged as camera target
        // 2. Snap camera position to target entity position
        // 3. Render
    }

    @Override
    public void close() throws Exception {

    }
}
