package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequirementsBuilderImpl implements RequirementsBuilder {
    private final SystemRequirements.Builder requirements = SystemRequirements.builder();
    private final SystemDependencyResolver dependencyBuilder;
    private final ECSSystem instance;
    private final SystemContext.Builder builder = new SystemContext.Builder();

    public SystemContext build() {
        return builder.instance(this.instance)
                      .requirements(this.requirements.build())
                      .dependencies(this.dependencyBuilder.buildFor(this.instance))
                      .build();
    }


    @Override
    public RequirementsBuilder tickAfter(@NonNull final Class<? extends ECSSystem> other) {
        this.dependencyBuilder.tickAfter(this.instance, other);
        return this;
    }

    @Override
    public RequirementsBuilder tickBefore(@NonNull final Class<? extends ECSSystem> other) {
        this.dependencyBuilder.tickBefore(this.instance, other);
        return this;
    }

    @Override
    public RequirementsBuilder tickAfter(@NonNull final SystemGroup group) {
        this.dependencyBuilder.tickAfter(this.instance, group);
        return this;
    }

    @Override
    public RequirementsBuilder tickBefore(@NonNull final SystemGroup group) {
        this.dependencyBuilder.tickBefore(this.instance, group);
        return this;
    }

    @Override
    public RequirementsBuilder addToGroup(@NonNull final SystemGroup group) {
        this.dependencyBuilder.addToGroup(group, this.instance);
        this.builder.group(group);
        return this;
    }

    @Override
    public RequirementsBuilder withComponent(@NonNull final Class<? extends Component> componentClass) {
        this.requirements.requiredComponent(componentClass);
        return this;
    }

    @Override
    public RequirementsBuilder withoutComponent(@NonNull final Class<? extends Component> componentClass) {
        this.requirements.excludedComponent(componentClass);
        return this;
    }

    @Override
    public RequirementsBuilder withComponentFrom(@NonNull final ComponentGroup componentGroup) {
        this.requirements.requiredGroup(componentGroup);
        return this;
    }

    @Override
    public RequirementsBuilder withoutComponentsFrom(@NonNull final ComponentGroup componentGroup) {
        this.requirements.excludedGroup(componentGroup);
        return this;
    }

    @Override
    public RequirementsBuilder requireResource(@NonNull final Class<? extends Resource> resource) {
        this.requirements.requiredResource(resource);
        return this;
    }
}
