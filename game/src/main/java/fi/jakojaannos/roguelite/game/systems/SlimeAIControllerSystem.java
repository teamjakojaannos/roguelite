package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.CharacterStats;
import fi.jakojaannos.roguelite.game.data.components.SlimeAI;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.stream.Stream;

@Slf4j
public class SlimeAIControllerSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                .requireResource(Players.class)
                .withComponent(SlimeAI.class)
                .withComponent(CharacterInput.class)
                .withComponent(CharacterStats.class)
                .withComponent(Transform.class);
    }


    private final Vector2d tempPos = new Vector2d(),
            tempDir = new Vector2d();

    @Override
    public void tick(
            Stream<Entity> entities,
            World world,
            double delta
    ) {


        val entityManager = world.getEntityManager();
        val player = world.getResource(Players.class).player;
        if (player == null){
            return;
        }

        val opt = entityManager.getComponentOf(player, Transform.class);
        if (opt.isEmpty()) {
            LOG.debug("Error: cannot get player's Transform!");
            return;
        }

        val playerPos = new Vector2d();
        opt.get().getCenter(playerPos);

        entities.forEach(entity -> {
            val ai = entityManager.getComponentOf(entity, SlimeAI.class).get();
            val input = entityManager.getComponentOf(entity, CharacterInput.class).get();


            ai.airTime -= delta;
            ai.jumpCoolDown -= delta;

            if (ai.airTime > 0.0) {
                input.move.set(ai.jumpDir);
                return;
            }


            entityManager.getComponentOf(entity, Transform.class).get().getCenter(tempPos);
            val dist = playerPos.distanceSquared(tempPos);

            if (dist > ai.chaseRadiusSquared ||
                    dist < ai.targetRadiusSquared) {
                input.move.set(0);
                return;
            }

            if (ai.jumpCoolDown < 0.0) {
                LOG.debug("HOP!");

                tempDir.set(playerPos)
                        .sub(tempPos);

                ai.jumpDir.set(tempDir);
                ai.airTime = ai.setAirTimeCoolDown;
                ai.jumpCoolDown = ai.setJumpCoolDown;

                input.move.set(tempDir);
            } else {
                input.move.set(0);
            }
        });
    }
}
