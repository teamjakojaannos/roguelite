package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.CharacterStats;
import fi.jakojaannos.roguelite.game.data.components.StalkerAI;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.stream.Stream;

@Slf4j
public class StalkerAIControllerSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .requireResource(Players.class)
                    .withComponent(StalkerAI.class)
                    .withComponent(CharacterInput.class)
                    .withComponent(Transform.class)
                    .withComponent(CharacterStats.class);
    }

    private final Vector2d tmpDirection = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world,
            final double delta
    ) {
        val player = world.getResource(Players.class).player;
        if (player == null) {
            return;
        }

        var playerTransform = world.getEntityManager().getComponentOf(player, Transform.class);

        val playerPos = playerTransform.orElseThrow().position;

        entities.forEach(entity -> {
            val stalkerAI = world.getEntityManager().getComponentOf(entity, StalkerAI.class).orElseThrow();
            val characterInput = world.getEntityManager().getComponentOf(entity, CharacterInput.class).orElseThrow();
            val characterStats = world.getEntityManager().getComponentOf(entity, CharacterStats.class).orElseThrow();

            stalkerAI.airTime -= delta;
            stalkerAI.jumpCoolDown -= delta;

            if (stalkerAI.airTime > 0) {
                characterStats.speed = 18.0f;
                characterInput.move.set(stalkerAI.jumpDir);
                return;
            }

            val myPos = new Vector2d();
            world.getEntityManager().getComponentOf(entity, Transform.class).get()
                 .getCenter(myPos);
            tmpDirection.set(playerPos).sub(myPos);

            double distToPlayerSquared = tmpDirection.lengthSquared();

            if (distToPlayerSquared > stalkerAI.sneakRadiusSquared) {
                // outside players "view"
                characterStats.speed = 1.7f;
            } else if (distToPlayerSquared > stalkerAI.leapRadiusSquared) {
                // shh, be quiet, the player can hear us
                characterStats.speed = 0.3f;
            } else {
                if (stalkerAI.jumpCoolDown <= 0.0f) {
                    // RRAAARRGHWG! JUMP ON THEM!

                    stalkerAI.jumpDir.set(tmpDirection);
                    stalkerAI.jumpCoolDown = stalkerAI.jumpAbilityGoesCoolDownThisLong;
                    stalkerAI.airTime = 0.5f;

                } else {
                    characterStats.speed = 0.3f;
                }
            }

            characterInput.move.set(tmpDirection);
        });
    }
}
