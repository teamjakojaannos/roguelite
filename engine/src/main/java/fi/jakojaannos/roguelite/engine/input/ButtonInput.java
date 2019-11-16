package fi.jakojaannos.roguelite.engine.input;

import lombok.Getter;

public class ButtonInput {
    @Getter private final InputButton button;
    @Getter private final Action action;

    public static InputEvent pressed(InputButton button) {
        return new InputEvent(new ButtonInput(button, Action.PRESS));
    }

    public static InputEvent released(InputButton button) {
        return new InputEvent(new ButtonInput(button, Action.RELEASE));
    }

    public static InputEvent event(InputButton button, Action action) {
        return new InputEvent(new ButtonInput(button, action));
    }

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
