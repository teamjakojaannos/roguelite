package fi.jakojäännös.roguelite.engine.input;

import java.util.Queue;

public interface InputProvider {
    /**
     * Gets all input events gathered since last frame.
     *
     * @return queue containing all input events to be processed
     */
    Queue<InputEvent> pollEvents();

    /**
     * Mapper for assigning scancodes for keys. Scancodes are platform-specific but consistent over
     * time.
     *
     * @param key Key to generate scancode for
     *
     * @return platform-specific scancode for given key
     */
    int mapScancode(InputButton.Keyboard key);
}
