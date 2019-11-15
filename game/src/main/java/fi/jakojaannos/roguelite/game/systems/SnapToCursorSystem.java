package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.CrosshairTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class SnapToCursorSystem implements ECSSystem<GameState> {
    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return List.of(Transform.class, CrosshairTag.class);
    }

    @Override
    public void tick(
            Stream<Entity> entities,
            GameState state,
            double delta,
            Cluster cluster
    ) {
        entities.forEach(entity -> {
            val transform = state.world.getComponentOf(entity, Transform.class).get();
            transform.setPosition(state.mouseX * state.realViewWidth,
                                  state.mouseY * state.realViewHeight);
        });
    }
}
