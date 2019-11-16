package fi.jakojaannos.roguelite.game.view;

import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLWindow;
import fi.jakojaannos.roguelite.engine.view.GameRenderer;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.view.systems.EntityBoundsRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.SpriteRenderingSystem;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class RogueliteGameRenderer implements GameRenderer<GameState> {
    private final SystemDispatcher<GameState> rendererDispatcher;
    private final RogueliteCamera camera;

    public RogueliteGameRenderer(@NonNull String assetRoot, @NonNull LWJGLWindow window) {
        LOG.debug("Constructing GameRenderer...");
        LOG.debug("asset root: {}", assetRoot);


        this.camera = new RogueliteCamera(window.getWidth(), window.getHeight());
        val builder = new DispatcherBuilder<GameState>()
                .withSystem("render_sprites", new SpriteRenderingSystem(assetRoot, this.camera));

        if (DebugConfig.debugModeEnabled) {
            builder.withSystem("render_debug", new EntityBoundsRenderingSystem(assetRoot, this.camera));
        }

        this.rendererDispatcher = builder.build();

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
