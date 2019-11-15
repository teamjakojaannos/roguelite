package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class PlayerInputSystem implements ECSSystem<GameState> {
    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return List.of(CharacterInput.class, CharacterAbilities.class, PlayerTag.class);
    }

    @Override
    public void tick(
            Stream<Entity> entities,
            GameState state,
            double delta,
            Cluster cluster
    ) {
        val inputHorizontal = (state.inputRight ? 1 : 0) - (state.inputLeft ? 1 : 0);
        val inputVertical = (state.inputDown ? 1 : 0) - (state.inputUp ? 1 : 0);
        boolean inputAttack = state.inputAttack;

        entities.forEach(entity -> {
            val input = cluster.getComponentOf(entity, CharacterInput.class).get();
            val abilities = cluster.getComponentOf(entity, CharacterAbilities.class).get();
            input.move.set(inputHorizontal,
                           inputVertical);
            input.attack = inputAttack;
            abilities.attackTarget.set(state.mouseX * state.realViewWidth,
                                       state.mouseY * state.realViewHeight);
        });
    }
}
