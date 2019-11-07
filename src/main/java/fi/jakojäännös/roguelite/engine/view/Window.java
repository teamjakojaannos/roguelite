package fi.jakojäännös.roguelite.engine.view;

public interface Window {
    void addResizeCallback(ResizeCallback callback);

    interface ResizeCallback {
        void call(int width, int height);
    }
}
