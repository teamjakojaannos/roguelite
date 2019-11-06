package fi.jakojäännös.roguelite.engine.input;

import lombok.Getter;

public class InputEvent {
    @Getter private final int key;
    @Getter private final int scancode;
    @Getter private final Action action;

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
