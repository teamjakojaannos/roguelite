package fi.jakojaannos.roguelite.game.view;

import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLWindow;
import fi.jakojaannos.roguelite.engine.view.GameRenderer;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.view.systems.EntityBoundsRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.LevelRenderingSystem;
import fi.jakojaannos.roguelite.game.view.systems.SpriteRenderingSystem;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class RogueliteGameRenderer implements GameRenderer<GameState> {
    private final SystemDispatcher rendererDispatcher;
    private final RogueliteCamera camera;

    public RogueliteGameRenderer(@NonNull String assetRoot, @NonNull LWJGLWindow window) {
        LOG.debug("Constructing GameRenderer...");
        LOG.debug("asset root: {}", assetRoot);


        this.camera = new RogueliteCamera(window.getWidth(), window.getHeight());
        val builder = new DispatcherBuilder()
                .withSystem("render_level", new LevelRenderingSystem(assetRoot, this.camera))
                .withSystem("render_sprites", new SpriteRenderingSystem(assetRoot, this.camera), "render_level");

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

        // Snap camera to active camera
        val cameraEntity = state.getWorld().getResource(CameraProperties.class).cameraEntity;
        state.getWorld().getEntities().getComponentOf(cameraEntity, Camera.class)
             .ifPresent(cam -> this.camera.setPosition(cam.pos.x - this.camera.getViewportWidthInUnits() / 2.0,
                                                       cam.pos.y - this.camera.getViewportHeightInUnits() / 2.0));
        this.rendererDispatcher.dispatch(state.getWorld(), partialTickAlpha);
    }

    @Override
    public void close() throws Exception {
        this.rendererDispatcher.close();
    }
}
