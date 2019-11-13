package fi.jakojäännös.roguelite.engine.input;

import lombok.Getter;

public interface InputAxis {
    enum Mouse implements InputAxis {
        X,
        Y,
        X_POS,
        Y_POS,
    }
}
