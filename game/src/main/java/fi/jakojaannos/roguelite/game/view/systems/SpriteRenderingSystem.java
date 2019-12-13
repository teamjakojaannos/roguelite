package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.content.view.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLSpriteBatch;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLTexture;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Rectangled;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@Slf4j
public class SpriteRenderingSystem implements ECSSystem, AutoCloseable {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.tickAfter(LevelRenderingSystem.class)
                    .withComponent(Transform.class)
                    .withComponent(SpriteInfo.class);
    }

    private final LWJGLCamera camera;
    private final SpriteBatch<String, LWJGLCamera> batch;
    private final BiFunction<String, Integer, LWJGLTexture> textureResolver;
    private final SpriteRegistry<LWJGLTexture> sprites;

    public SpriteRenderingSystem(
            final Path assetRoot,
            final LWJGLCamera camera,
            final SpriteRegistry<LWJGLTexture> sprites
    ) {
        this.camera = camera;
        this.sprites = sprites;

        val batch = new LWJGLSpriteBatch(assetRoot, "sprite", sprites);
        this.batch = batch;
        this.textureResolver = (s, integer) -> batch.resolveTexture(s, integer).getTexture();
    }

    @Override
    public void tick(
            Stream<Entity> entities,
            World world,
            double partialTickAlpha
    ) {
        // Render using two-pass approach. By using correct data-structures with sensible estimates
        // for the initial capacity, the time complexity should be quite close to O(n). The process
        // simply put as follows:
        //  1. Gather required context on all entities we are going to render. While collecting
        //     the entities do:
        //      a. Group collected entities by z-layer
        //      b. Group collected entities by texture
        //  2. Render layers in ascending order using SpriteBatch
        //
        // Current implementation is quick 'n dumb unholy mess of streams and hash maps, which in
        // turn is, most likely very, very inefficient, both memory- and CPU -wise.
        val renderQueue = new HashMap<Integer, HashMap<LWJGLTexture, List<SpriteRenderEntry>>>();
        entities.forEach(
                entity -> {
                    val transform = world.getEntityManager().getComponentOf(entity, Transform.class).get();
                    val info = world.getEntityManager().getComponentOf(entity, SpriteInfo.class).get();

                    val texturesForZLayer = renderQueue.computeIfAbsent(info.zLayer,
                                                                        zLayer -> new HashMap<>());
                    val texture = this.textureResolver.apply(info.spriteName, info.getCurrentFrame());
                    val spritesForTexture = texturesForZLayer.computeIfAbsent(texture,
                                                                              tex -> new ArrayList<>());

                    val bounds = world.getEntityManager().getComponentOf(entity, Collider.class)
                                      .map(collider -> new Rectangled(transform.position.x - 0 * collider.origin.x,
                                                                      transform.position.y - 0 * collider.origin.y,
                                                                      transform.position.x - 0 * collider.origin.x + collider.width,
                                                                      transform.position.y - 0 * collider.origin.y + collider.height))
                                      .orElseGet(() -> new Rectangled(transform.position.x,
                                                                      transform.position.y,
                                                                      transform.position.x + 1.0,
                                                                      transform.position.y + 1.0));

                    val width = bounds.maxX - bounds.minX;
                    val height = bounds.maxY - bounds.minY;
                    spritesForTexture.add(new SpriteRenderEntry(info.spriteName,
                                                                info.getCurrentFrame(),
                                                                info.zLayer,
                                                                bounds.minX,
                                                                bounds.minY,
                                                                width,
                                                                height));
                }
        );

        this.batch.begin(this.camera);
        renderQueue.keySet()
                   .stream()
                   .sorted()
                   .map(renderQueue::get)
                   .forEach(spritesForTexture ->
                                    spritesForTexture.forEach((texture, entries) ->
                                                                      entries.forEach(entry -> this.batch.draw(entry.getSpriteId(),
                                                                                                               entry.getFrame(),
                                                                                                               entry.getX(),
                                                                                                               entry.getY(),
                                                                                                               entry.getWidth(),
                                                                                                               entry.getHeight()))));
        this.batch.end();
    }

    @Override
    public void close() throws Exception {
        this.batch.close();
    }

    @RequiredArgsConstructor
    private static class SpriteRenderEntry {
        @Getter private final String spriteId;
        @Getter private final int frame;
        @Getter private final int zLayer;
        @Getter private final double x, y;
        @Getter private final double width, height;
    }
}
