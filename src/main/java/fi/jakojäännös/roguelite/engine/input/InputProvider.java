package fi.jakojäännös.roguelite.engine.input;

import java.util.ArrayDeque;
import java.util.Queue;

public interface InputProvider {
    Queue<InputEvent> pollEvents();
}
