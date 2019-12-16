package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.data.resources.Time;
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
            tempPos2 = new Vector2d(),
            tempPlayerPos = new Vector2d(),
            tempDir = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val delta = world.getResource(Time.class).getTimeStepInSeconds();

        val entityManager = world.getEntityManager();
        val player = world.getResource(Players.class).player;
        if (player == null) {
            return;
        }

        val opt = entityManager.getComponentOf(player, Transform.class);
        if (opt.isEmpty()) {
            LOG.debug("Error: cannot get player's Transform!");
            return;
        }

        tempPlayerPos.set(opt.get().position);

        entities.forEach(entity -> {
            val ai = entityManager.getComponentOf(entity, SlimeAI.class).orElseThrow();
            val input = entityManager.getComponentOf(entity, CharacterInput.class).orElseThrow();
            val stats = entityManager.getComponentOf(entity, CharacterStats.class).orElseThrow();
            tempPos.set(entityManager.getComponentOf(entity, Transform.class).orElseThrow().position);


            ai.airTime -= delta;
            ai.jumpCoolDown -= delta;

            if (ai.airTime > 0.0) {
                input.move.set(ai.jumpDir);
                return;
            }

            val optSharedAI = entityManager.getComponentOf(entity, SlimeSharedAI.class);
            if (optSharedAI.isPresent()) {
                val sharedAI = optSharedAI.get();

                // in case everyone else dies
                if (sharedAI.slimes.size() <= 1) {
                    sharedAI.slimes.clear();
                    entityManager.removeComponentFrom(entity, SlimeSharedAI.class);

                    return;
                }


                if (sharedAI.regrouping) {
                    moveToRegroupArea(sharedAI, ai, input, tempPos, stats);
                    return;
                } else {
                    // check if everyone is ready to regroup
                    doAICheck(sharedAI, entityManager);
                }

            }


            val dist = tempPlayerPos.distanceSquared(tempPos);


            if (dist > ai.chaseRadiusSquared) {
                ai.regroupTimer -= delta;

                input.move.set(0);
                return;
            }


            if (dist < ai.targetRadiusSquared) {
                input.move.set(0);
                return;
            }

            if (ai.jumpCoolDown < 0.0) {
                tempDir.set(tempPlayerPos)
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


    private void doAICheck(SlimeSharedAI sharedAI, EntityManager entityManager) {

        boolean everyoneReady = true;

        for (int i = sharedAI.slimes.size() - 1; i >= 0; i--) {
            val slime = sharedAI.slimes.get(i);
            val optAi = entityManager.getComponentOf(slime, SlimeAI.class);


            if (optAi.isEmpty()) {
                LOG.debug("Removing a faulty slime... (missing slime ai)"); // TODO: better message here
                sharedAI.slimes.remove(i);
                continue;
            }

            val optPos = entityManager.getComponentOf(slime, Transform.class);
            if (optPos.isEmpty()) {
                LOG.debug("Removing a faulty slime... (missing transform)"); // TODO: better message here
                sharedAI.slimes.remove(i);
                continue;
            }

            val ai = optAi.get();
            if (ai.regroupTimer > 0.0) {
                everyoneReady = false;
            }
        }


        if (everyoneReady) {
            sharedAI.regrouping = true;
            tempPos2.set(0.0, 0.0);

            for (val slime : sharedAI.slimes) {
                tempPos2.set(entityManager.getComponentOf(slime, Transform.class).orElseThrow().position);
                sharedAI.regroupPos.add(tempPos2.mul(1.0 / sharedAI.slimes.size()));
            }

        }
    }


    private void moveToRegroupArea(
            SlimeSharedAI sharedAI,
            SlimeAI ai,
            CharacterInput input,
            Vector2d myPos,
            CharacterStats stats
    ) {
        if (sharedAI.regroupPos.distanceSquared(myPos) <= ai.regroupRadiusSquared) {
            input.move.set(0);
            return;
        }


        stats.speed = ai.crawlSpeed;
        tempDir.set(sharedAI.regroupPos)
                .sub(myPos);

        input.move.set(tempDir);
    }


}
