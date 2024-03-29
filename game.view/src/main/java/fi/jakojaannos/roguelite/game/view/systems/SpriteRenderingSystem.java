package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLSpriteBatch;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLTexture;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@Slf4j
public class SpriteRenderingSystem implements ECSSystem, AutoCloseable {
    private static final Vector2d ZERO_VECTOR = new Vector2d(0.0);

    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.tickAfter(LevelRenderingSystem.class)
                    .withComponent(Transform.class)
                    .withComponent(SpriteInfo.class);
    }

    private final LWJGLCamera camera;
    private final SpriteBatch<String, LWJGLCamera> batch;
    private final BiFunction<String, Integer, LWJGLTexture> textureResolver;

    public SpriteRenderingSystem(
            final Path assetRoot,
            final LWJGLCamera camera,
            final SpriteRegistry<LWJGLTexture> sprites
    ) {
        this.camera = camera;

        val batch = new LWJGLSpriteBatch(assetRoot, "sprite", sprites);
        this.batch = batch;
        this.textureResolver = (spriteName, frame) -> batch.resolveTexture(spriteName, frame).getTexture();
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
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
                    val transform = world.getEntityManager().getComponentOf(entity, Transform.class).orElseThrow();
                    val info = world.getEntityManager().getComponentOf(entity, SpriteInfo.class).orElseThrow();

                    val texturesForZLayer = renderQueue.computeIfAbsent(info.zLayer,
                                                                        zLayer -> new HashMap<>());
                    val texture = this.textureResolver.apply(info.spriteName, info.getCurrentFrame());
                    val spritesForTexture = texturesForZLayer.computeIfAbsent(texture,
                                                                              tex -> new ArrayList<>());

                    val maybeCollider = world.getEntityManager().getComponentOf(entity, Collider.class);
                    val origin = maybeCollider.map(collider -> collider.origin)
                                              .orElse(ZERO_VECTOR);
                    val size = maybeCollider.map(collider -> new Vector2d(collider.width,
                                                                          collider.height))
                                            .orElse(ZERO_VECTOR);
                    val position = transform.position;

                    spritesForTexture.add(new SpriteRenderEntry(info.spriteName,
                                                                info.getCurrentFrame(),
                                                                info.zLayer,
                                                                position.x,
                                                                position.y,
                                                                origin.x,
                                                                origin.y,
                                                                size.x,
                                                                size.y,
                                                                transform.rotation));
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
                                                                                                               entry.getOriginX(),
                                                                                                               entry.getOriginY(),
                                                                                                               entry.getWidth(),
                                                                                                               entry.getHeight(),
                                                                                                               entry.getRotation()))));
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
        @Getter private final double originX, originY;
        @Getter private final double width, height;
        @Getter private final double rotation;
    }
}
