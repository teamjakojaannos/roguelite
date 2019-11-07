package fi.jakojäännös.roguelite.engine.view;

public interface GameRenderer<TState> extends AutoCloseable {
    void render(TState state, double delta);
}
