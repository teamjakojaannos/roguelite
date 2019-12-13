package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.DeadTag;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import lombok.val;

import java.util.stream.Stream;

public class CleanUpDeadPlayersSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .withComponent(DeadTag.class)
                    .tickBefore(ReaperSystem.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world,
            final double delta
    ) {
        val players = world.getResource(Players.class);
        if (players.player == null) {
            return;
        }

        entities.filter(entity -> entity.getId() == players.player.getId())
                .forEach(entity -> players.player = null);
    }
}
