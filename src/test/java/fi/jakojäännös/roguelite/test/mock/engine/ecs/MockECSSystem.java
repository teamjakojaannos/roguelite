package fi.jakojäännös.roguelite.test.mock.engine.ecs;

import fi.jakojäännös.roguelite.engine.ecs.Component;
import fi.jakojäännös.roguelite.engine.ecs.ECSSystem;
import fi.jakojäännös.roguelite.engine.ecs.Entity;
import fi.jakojäännös.roguelite.game.data.GameState;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class MockECSSystem implements ECSSystem {
    private Collection<Class<? extends Component>> components;
    public boolean tickCalled = false;

    public MockECSSystem() {
        this(List.of());
    }

    public MockECSSystem(Collection<Class<? extends Component>> components) {
        this.components = components;
    }

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return this.components;
    }

    @Override
    public void tick(
            Stream<Entity> entities, GameState state, double delta
    ) {
        this.tickCalled = true;
    }
}
