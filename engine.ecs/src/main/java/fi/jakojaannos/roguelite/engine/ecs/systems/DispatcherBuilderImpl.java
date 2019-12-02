package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import lombok.NonNull;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DispatcherBuilderImpl
        implements fi.jakojaannos.roguelite.engine.ecs.DispatcherBuilder {
    private final List<RequirementsBuilderImpl> systems = new ArrayList<>();
    private final SystemDependencyResolver dependencyResolver = new SystemDependencyResolver();

    @Override
    public DispatcherBuilder withGroup(@NonNull final SystemGroup group) {
        this.dependencyResolver.addGroup(group);
        return this;
    }

    @Override
    public DispatcherBuilder addGroupDependency(
            @NonNull final SystemGroup group,
            @NonNull final SystemGroup dependency
    ) {
        if (group.equals(dependency)) {
            throw new IllegalStateException("Group cannot depend on itself!");
        }

        this.dependencyResolver.addGroupDependency(group, dependency);
        return this;
    }

    @Override
    public DispatcherBuilder withSystem(@NonNull final ECSSystem system) {
        val requirementsBuilder = new RequirementsBuilderImpl(this.dependencyResolver, system);
        this.systems.add(requirementsBuilder);
        system.declareRequirements(requirementsBuilder);
        this.dependencyResolver.addInstance(system);
        return this;
    }

    @Override
    public SystemDispatcher build() {
        return new SystemDispatcherImpl(
                new SystemStorage(this.systems.stream()
                                              .map(RequirementsBuilderImpl::build)
                                              .collect(Collectors.toList()),
                                  this.dependencyResolver.buildGroups()));
    }
}
