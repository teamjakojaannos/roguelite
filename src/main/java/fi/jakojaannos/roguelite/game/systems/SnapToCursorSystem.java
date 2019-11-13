package fi.jakojäännös.roguelite.game.systems;

import fi.jakojäännös.roguelite.engine.ecs.Cluster;
import fi.jakojäännös.roguelite.engine.ecs.Component;
import fi.jakojäännös.roguelite.engine.ecs.ECSSystem;
import fi.jakojäännös.roguelite.engine.ecs.Entity;
import fi.jakojäännös.roguelite.game.data.GameState;
import fi.jakojäännös.roguelite.game.data.components.CrosshairTag;
import fi.jakojäännös.roguelite.game.data.components.Position;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class SnapToCursorSystem implements ECSSystem<GameState> {
    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return List.of(Position.class, CrosshairTag.class);
    }

    @Override
    public void tick(
            Stream<Entity> entities,
            GameState state,
            double delta,
            Cluster cluster
    ) {
        entities.forEach(entity -> state.world.getComponentOf(entity, Position.class)
                                              .ifPresent(position -> {
                                                  position.x = state.mouseX * state.realViewWidth;
                                                  position.y = state.mouseY * state.realViewHeight;
                                              }));
    }
}
