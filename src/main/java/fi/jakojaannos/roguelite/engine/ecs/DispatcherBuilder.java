package fi.jakojaannos.roguelite.engine.ecs;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

public class DispatcherBuilder<TState> {
    private Collection<SystemEntry<TState>> systems = new ArrayList<>();

    public DispatcherBuilder<TState> withSystem(
            @NonNull String name,
            @NonNull ECSSystem<TState> system,
            @NonNull String... dependencies
    ) {
        this.systems.add(new SystemEntry<>(name, system, dependencies));
        return this;
    }

    public SystemDispatcher<TState> build() {
        return new SystemDispatcher<>(this.systems);
    }

    @RequiredArgsConstructor
    static class SystemEntry<TState> {
        @Getter private final String name;
        @Getter private final ECSSystem<TState> system;
        @Getter private final String[] dependencies;
    }
}
