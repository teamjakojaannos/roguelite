package fi.jakojäännös.roguelite.engine.ecs;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

public class DispatcherBuilder {
    private Collection<SystemEntry> systems = new ArrayList<>();
    private Cluster cluster;

    public DispatcherBuilder withCluster(@NonNull Cluster cluster) {
        this.cluster = cluster;
        return this;
    }

    public DispatcherBuilder withSystem(
            @NonNull String name,
            @NonNull ECSSystem system,
            @NonNull String... dependencies
    ) {
        this.systems.add(new SystemEntry(name, system, dependencies));
        return this;
    }

    public SystemDispatcher build() {
        return new SystemDispatcher(this.cluster, this.systems);
    }

    @RequiredArgsConstructor
    static class SystemEntry {
        @Getter private final String name;
        @Getter private final ECSSystem system;
        @Getter private final String[] dependencies;
    }
}
