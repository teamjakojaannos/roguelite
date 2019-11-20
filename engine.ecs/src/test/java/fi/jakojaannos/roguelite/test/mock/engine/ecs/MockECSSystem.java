package fi.jakojaannos.roguelite.test.mock.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import lombok.NonNull;

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
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        this.tickCalled = true;
    }
}
