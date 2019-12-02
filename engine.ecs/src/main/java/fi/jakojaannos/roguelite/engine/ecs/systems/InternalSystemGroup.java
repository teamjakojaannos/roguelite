package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.util.Collection;
import java.util.stream.Stream;

@Builder(builderClassName = "Builder")
public class InternalSystemGroup {
    @Getter @NonNull private final SystemGroup group;
    @Singular @NonNull private final Collection<ECSSystem> systems;
    @Singular @NonNull private final Collection<Class<? extends ECSSystem>> dependencies;
    @Singular @NonNull private final Collection<SystemGroup> groupDependencies;

    public Stream<Class<? extends ECSSystem>> getSystems() {
        return this.systems.stream()
                           .map(ECSSystem::getClass);
    }

    public Stream<Class<? extends ECSSystem>> getDependencies() {
        return this.dependencies.stream();
    }

    public Stream<SystemGroup> getGroupDependencies() {
        return this.groupDependencies.stream();
    }
}
