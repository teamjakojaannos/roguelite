package fi.jakojaannos.roguelite.engine.input;

import lombok.Getter;

public class ButtonInput {
    @Getter private final InputButton button;
    @Getter private final Action action;

    public ButtonInput(InputButton button, Action action) {
        this.button = button;
        this.action = action;
    }

    public enum Action {
        PRESS,
        RELEASE,
        REPEAT,
    }
}
