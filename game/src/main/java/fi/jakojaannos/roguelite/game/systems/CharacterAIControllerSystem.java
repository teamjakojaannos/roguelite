package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class CharacterAIControllerSystem implements ECSSystem<GameState> {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            CharacterInput.class, EnemyAI.class, Transform.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    private final Vector2d tmpDirection = new Vector2d();

    @Override
    public void tick(Stream<Entity> entities, GameState gameState, double delta, Cluster cluster) {
        Vector2d playerPos = new Vector2d();
        cluster.getComponentOf(gameState.player, Transform.class)
                .orElse(new Transform(5.0f, 5.0f))
                .getCenter(playerPos);

        entities.forEach(entity -> {
            val myPos = new Vector2d();
            cluster.getComponentOf(entity, Transform.class).get()
                    .getCenter(myPos);

            tmpDirection.set(playerPos).sub(myPos);
            val input = cluster.getComponentOf(entity, CharacterInput.class).get();
            input.move.set(tmpDirection);
        });
    }
}
