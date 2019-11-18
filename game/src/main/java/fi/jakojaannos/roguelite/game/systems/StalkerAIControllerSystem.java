package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.CharacterStats;
import fi.jakojaannos.roguelite.game.data.components.StalkerAI;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class StalkerAIControllerSystem implements ECSSystem<GameState> {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            CharacterInput.class, StalkerAI.class, Transform.class, CharacterStats.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }


    private final Vector2d tmpDirection = new Vector2d();

    @Override
    public void tick(
            Stream<Entity> entities,
            GameState gameState,
            double delta,
            Cluster cluster
    ) {

        var opt = cluster.getComponentOf(gameState.player, Transform.class);
        if (opt.isEmpty()) return;

        val playerPos = new Vector2d();
        opt.get().getCenter(playerPos);

        entities.forEach(entity -> {
            val stalkerAI = cluster.getComponentOf(entity, StalkerAI.class).get();
            val characterInput = cluster.getComponentOf(entity, CharacterInput.class).get();
            val characterStats = cluster.getComponentOf(entity, CharacterStats.class).get();

            stalkerAI.airTime -= delta;
            stalkerAI.jumpCoolDown -= delta;


            if (stalkerAI.airTime > 0) {
                characterStats.speed = 18.0f;
                characterInput.move.set(stalkerAI.jumpDir);
                return;
            }


            val myPos = new Vector2d();
            cluster.getComponentOf(entity, Transform.class).get()
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
