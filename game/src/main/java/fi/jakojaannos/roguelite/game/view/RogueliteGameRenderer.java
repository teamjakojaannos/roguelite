package fi.jakojaannos.roguelite.game.view;

import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLWindow;
import fi.jakojaannos.roguelite.engine.view.GameRenderer;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.view.systems.PlayerRendererSystem;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RogueliteGameRenderer implements GameRenderer<GameState> {
    private final SystemDispatcher<GameState> rendererDispatcher;
    private final RogueliteCamera camera;

    public RogueliteGameRenderer(@NonNull String assetRoot, @NonNull LWJGLWindow window) {
        LOG.info("Constructing GameRenderer...");
        LOG.info("asset root: {}", assetRoot);


        this.camera = new RogueliteCamera();
        this.rendererDispatcher = new DispatcherBuilder<GameState>()
                .withSystem("render_player", new PlayerRendererSystem(assetRoot, this.camera))
                .build();

        window.addResizeCallback(this.camera::resizeViewport);

        LOG.info("GameRenderer initialization finished.");
    }

    @Override
    public void render(GameState state, double partialTickAlpha) {
        // Make sure that the camera configuration matches the current state
        this.camera.updateConfigurationFromState(state);

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
