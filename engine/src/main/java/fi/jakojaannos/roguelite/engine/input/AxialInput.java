package fi.jakojaannos.roguelite.engine.input;

import lombok.Getter;

public class AxialInput {
    @Getter private final InputAxis axis;
    @Getter private final double value;

    public AxialInput(InputAxis axis, double value) {
        this.axis = axis;
        this.value = value;
    }
}
