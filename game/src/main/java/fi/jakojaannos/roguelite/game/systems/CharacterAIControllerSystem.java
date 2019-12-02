package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.FollowerEnemyAI;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.stream.Stream;

@Slf4j
public class CharacterAIControllerSystem implements ECSSystem {
    @Override
    public void declareRequirements(@NonNull RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .requireResource(Players.class)
                    .withComponent(FollowerEnemyAI.class)
                    .withComponent(CharacterInput.class)
                    .withComponent(Transform.class);
    }

    private final Vector2d tmpDirection = new Vector2d();

    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        val player = world.getResource(Players.class).player;
        val playerPos = new Vector2d();
        world.getEntityManager().getComponentOf(player, Transform.class)
             .orElse(new Transform(5.0f, 5.0f))
             .getCenter(playerPos);

        entities.forEach(entity -> {
            val aiPos = new Vector2d();
            world.getEntityManager().getComponentOf(entity, Transform.class).get()
                 .getCenter(aiPos);

            tmpDirection.set(playerPos).sub(aiPos);
            val input = world.getEntityManager().getComponentOf(entity, CharacterInput.class).get();
            input.move.set(tmpDirection);
        });
    }
}
