package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Everything known about a system. More specifically everything that ever needs to be known from
 * the {@link fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher dispatcher} point of view, at
 * least.
 */
@Builder(builderClassName = "Builder")
public final class SystemContext {
    @Getter @NonNull private final SystemRequirements requirements;
    @Getter @NonNull private final SystemDependencies dependencies;
    @Getter @NonNull private final ECSSystem instance;

    @Singular @NonNull private final Collection<SystemGroup> groups;

    public Stream<SystemGroup> getGroups() {
        return this.groups.stream();
    }
}
