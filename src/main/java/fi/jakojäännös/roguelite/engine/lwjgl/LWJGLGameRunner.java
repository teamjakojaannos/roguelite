package fi.jakojäännös.roguelite.engine.lwjgl;

import fi.jakojäännös.roguelite.engine.Game;
import fi.jakojäännös.roguelite.engine.input.InputProvider;
import fi.jakojäännös.roguelite.engine.GameRunner;

public class LWJGLGameRunner<TGame extends Game, TInput extends InputProvider> extends GameRunner<TGame, TInput> {
    @Override
    public void close() throws Exception {

    }
}
