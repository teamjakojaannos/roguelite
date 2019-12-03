package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLSpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.TileMapLayer;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import lombok.val;

import java.util.stream.Stream;

public class LevelRenderingSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.withComponent(TileMapLayer.class);
    }

    private final LWJGLCamera camera;
    private final SpriteBatch<String, LWJGLCamera> batch;

    public LevelRenderingSystem(final String assetRoot, final LWJGLCamera camera) {
        this.camera = camera;
        this.batch = new LWJGLSpriteBatch(assetRoot, "sprite");
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world,
            final double partialTickAlpha
    ) {
        val camera = world.getEntityManager()
                          .getComponentOf(world.getResource(CameraProperties.class).cameraEntity,
                                          Camera.class)
                          .get();

        val regionX = (int) Math.floor(camera.pos.x - this.camera.getViewportWidthInUnits() / 2.0);
        val regionY = (int) Math.floor(camera.pos.y - this.camera.getViewportHeightInUnits() / 2.0);
        val regionW = (int) Math.ceil(this.camera.getViewportWidthInUnits()) + 1;
        val regionH = (int) Math.ceil(this.camera.getViewportHeightInUnits()) + 1;

        this.batch.begin(this.camera);
        entities.forEach(entity -> {
            val level = world.getEntityManager().getComponentOf(entity, TileMapLayer.class).get();

            val tileSize = 1.0;
            for (int x = regionX; x < regionX + regionW; ++x) {
                for (int y = regionY; y < regionY + regionH; ++y) {
                    val tile = level.tileMap.getTile(x, y);
                    this.batch.draw("sprites/tileset",
                                    tile.getTypeIndex(),
                                    x * tileSize,
                                    y * tileSize,
                                    tileSize,
                                    tileSize);
                }
            }
        });
        this.batch.end();
    }
}
