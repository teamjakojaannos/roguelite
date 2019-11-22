package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;

import java.util.ArrayList;
import java.util.List;

public class Collider implements Component {

    public final List<CollisionEvent> collisions;

    public Collider(){
        this.collisions = new ArrayList<>();
    }


}
