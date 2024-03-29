package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.state.TimeProvider;
import fi.jakojaannos.roguelite.game.data.components.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.Time;
import lombok.val;

import java.util.stream.Stream;

public class EnemyAttackCoolDownSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .withComponent(Transform.class)
                    .withComponent(CharacterAbilities.class)
                    .withComponent(EnemyTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val delta = world.getResource(Time.class).getTimeStepInSeconds();

        val entityManager = world.getEntityManager();
        entities.forEach(entity -> entityManager.getComponentOf(entity, CharacterAbilities.class)
                                                .orElseThrow().attackTimer += delta);
    }
}
