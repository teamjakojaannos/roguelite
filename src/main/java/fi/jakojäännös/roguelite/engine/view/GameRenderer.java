package fi.jakojäännös.roguelite.engine.view;

import fi.jakojäännös.roguelite.engine.Game;

public interface GameRenderer<TGame extends Game> {
    void render(TGame game, double delta);
}
