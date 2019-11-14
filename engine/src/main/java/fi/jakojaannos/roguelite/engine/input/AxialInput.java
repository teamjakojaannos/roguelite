package fi.jakojaannos.roguelite.engine.input;

import lombok.Getter;

public class AxialInput {
    @Getter private final InputAxis axis;
    @Getter private final float value;

    public AxialInput(InputAxis axis, float value) {
        this.axis = axis;
        this.value = value;
    }
}
