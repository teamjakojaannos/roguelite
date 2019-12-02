package fi.jakojaannos.roguelite.engine.ecs;

import java.util.Collection;

public interface ComponentGroup {
    int getId();

    String getName();

    Collection<Class<? extends Component>> getComponentTypes();
}
