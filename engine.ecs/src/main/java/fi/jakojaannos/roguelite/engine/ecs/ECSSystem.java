package fi.jakojaannos.roguelite.engine.ecs;

import lombok.NonNull;

import java.util.stream.Stream;

public interface ECSSystem {
    void declareRequirements(@NonNull RequirementsBuilder requirements);

    void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    );
}
