package fi.jakojäännös.roguelite.game.data.components;

import fi.jakojäännös.roguelite.engine.ecs.Component;

public class Position implements Component {
    public float x;
    public float y;

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
