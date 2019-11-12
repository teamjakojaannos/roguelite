package fi.jakojäännös.roguelite.game.view;

import fi.jakojäännös.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojäännös.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojäännös.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojäännös.roguelite.engine.lwjgl.view.LWJGLWindow;
import fi.jakojäännös.roguelite.engine.view.GameRenderer;
import fi.jakojäännös.roguelite.game.data.GameState;
import fi.jakojäännös.roguelite.game.view.systems.PlayerRendererSystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RogueliteGameRenderer implements GameRenderer<GameState> {
    private final SystemDispatcher<GameState> rendererDispatcher;
    private final LWJGLCamera camera;

    public RogueliteGameRenderer(LWJGLWindow window) {
        LOG.info("Constructing GameRenderer...");

        this.camera = new LWJGLCamera();
        this.rendererDispatcher = new DispatcherBuilder<GameState>()
                .withSystem("render_player", new PlayerRendererSystem(this.camera))
                .build();

        window.addResizeCallback(this.camera::resizeViewport);

        LOG.info("GameRenderer initialization finished.");
    }

    @Override
    public void render(GameState state, double partialTickAlpha) {
        // Make sure that the camera configuration matches the current state
        //this.camera.updateConfigurationFromState(state);

        // 1. Find entity tagged as camera target
        // 2. Snap camera position to target entity position
        // 3. Render

        this.rendererDispatcher.dispatch(state.world, state, partialTickAlpha);
    }

    @Override
    public void close() throws Exception {
        this.rendererDispatcher.close();
    }
}
