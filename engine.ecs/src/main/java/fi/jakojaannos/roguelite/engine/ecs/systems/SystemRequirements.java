package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ComponentGroup;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.ecs.World;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Runtime requirements of a system. These are resources the system consumes by
 * reading/writing/mutating over and can assume to be available when executing its {@link
 * fi.jakojaannos.roguelite.engine.ecs.ECSSystem#tick(Stream, World, double) tick} function.
 */
@Builder(builderClassName = "Builder")
class SystemRequirements {
    @Getter @Singular private final Collection<Class<? extends Component>> requiredComponents;
    @Getter @Singular private final Collection<Class<? extends Component>> excludedComponents;
    @Getter @Singular private final Collection<ComponentGroup> requiredGroups;
    @Getter @Singular private final Collection<ComponentGroup> excludedGroups;
    @Getter @Singular private final Collection<Class<? extends Resource>> requiredResources;
}
