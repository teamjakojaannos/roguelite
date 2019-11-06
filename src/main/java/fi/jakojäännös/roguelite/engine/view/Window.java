package fi.jakojäännös.roguelite.engine.view;

public interface Window {
    void setResizeCallback(ResizeCallback callback);

    interface ResizeCallback {
        void call(int width, int height);
    }
}
