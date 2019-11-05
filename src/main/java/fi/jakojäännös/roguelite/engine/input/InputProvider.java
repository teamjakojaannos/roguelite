package fi.jakojäännös.roguelite.engine.input;

import java.util.ArrayDeque;
import java.util.Queue;

public class InputProvider {
    public Queue<InputEvent> pollEvents() {
        return new ArrayDeque<>();
    }
}
