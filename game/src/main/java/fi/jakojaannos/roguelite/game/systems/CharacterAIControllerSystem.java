package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.EnemyAI;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class CharacterAIControllerSystem implements ECSSystem {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            CharacterInput.class, EnemyAI.class, Transform.class
    );

    private static final List<Class<? extends Resource>> REQUIRED_RESOURCES = List.of(
            Players.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    @Override
    public Collection<Class<? extends Resource>> getRequiredResources() {
        return REQUIRED_RESOURCES;
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
        world.getEntities().getComponentOf(player, Transform.class)
             .orElse(new Transform(5.0f, 5.0f))
             .getCenter(playerPos);

        entities.forEach(entity -> {
            val aiPos = new Vector2d();
            world.getEntities().getComponentOf(entity, Transform.class).get()
                 .getCenter(aiPos);

            tmpDirection.set(playerPos).sub(aiPos);
            val input = world.getEntities().getComponentOf(entity, CharacterInput.class).get();
            input.move.set(tmpDirection);
        });
    }
}
