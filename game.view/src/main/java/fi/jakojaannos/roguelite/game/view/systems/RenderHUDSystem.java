package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLTexture;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.TextRenderer;

import java.util.stream.Stream;

public class RenderHUDSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.tickAfter(SpriteRenderingSystem.class);
    }

    private final TextRenderer<LWJGLTexture> textRenderer;

    public RenderHUDSystem(final TextRenderer<LWJGLTexture> textRenderer) {
        this.textRenderer = textRenderer;
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world,
            final double delta
    ) {
        this.textRenderer.draw(-12, -5, 4, "This is some test text\n(in-world)");
    }
}
