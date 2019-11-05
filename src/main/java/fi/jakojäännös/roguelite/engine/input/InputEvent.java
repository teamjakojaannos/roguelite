package fi.jakojäännös.roguelite.engine.input;

public class InputEvent {
    private final int key;
    private final int scancode;
    private final Action action;

    public InputEvent(int key, int scancode, Action action) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
    }

    public enum Action {
        PRESS,
        RELEASE,
        REPEAT,
    }
}
