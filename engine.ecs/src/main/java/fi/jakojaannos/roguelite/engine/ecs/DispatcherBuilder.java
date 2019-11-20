package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.systems.SystemDispatcherImpl;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

public class DispatcherBuilder {
    private Collection<SystemEntry> systems = new ArrayList<>();

    public DispatcherBuilder withSystem(
            @NonNull String name,
            @NonNull ECSSystem system,
            @NonNull String... dependencies
    ) {
        this.systems.add(new SystemEntry(name, system, dependencies));
        return this;
    }

    public SystemDispatcher build() {
        return new SystemDispatcherImpl(this.systems);
    }

    @RequiredArgsConstructor
    public static class SystemEntry {
        @Getter private final String name;
        @Getter private final ECSSystem system;
        @Getter private final String[] dependencies;
    }
}
