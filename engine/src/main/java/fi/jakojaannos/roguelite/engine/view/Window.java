package fi.jakojaannos.roguelite.engine.view;

public interface Window {
    enum Mode {
        Windowed,
        FullScreen,
        Borderless,
    }

    void addResizeCallback(ResizeCallback callback);

    interface ResizeCallback {
        void call(int width, int height);
    }
}
