package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import lombok.NonNull;
import lombok.val;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class PlayerInputSystem implements ECSSystem {
    private static final List<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            CharacterInput.class,
            CharacterAbilities.class,
            PlayerTag.class
    );

    private static final List<Class<? extends Resource>> REQUIRED_RESOURCES = List.of(
            Inputs.class, Mouse.class, CameraProperties.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    @Override
    public Collection<Class<? extends Resource>> getRequiredResources() {
        return REQUIRED_RESOURCES;
    }

    private final Vector2d tmpCursorPos = new Vector2d();

    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        val inputs = world.getResource(Inputs.class);
        val mouse = world.getResource(Mouse.class);
        val camProps = world.getResource(CameraProperties.class);
        val cursorPosition = tmpCursorPos.set(0.0, 0.0);
        if (camProps.cameraEntity != null) {
            val camera = world.getEntityManager().getComponentOf(camProps.cameraEntity, Camera.class).get();
            mouse.calculateCursorPositionRelativeToCamera(camera, camProps, tmpCursorPos);
        } else {
            cursorPosition.set(mouse.pos.x * camProps.viewportWidthInWorldUnits,
                               mouse.pos.y * camProps.viewportHeightInWorldUnits);
        }

        val inputHorizontal = (inputs.inputRight ? 1 : 0) - (inputs.inputLeft ? 1 : 0);
        val inputVertical = (inputs.inputDown ? 1 : 0) - (inputs.inputUp ? 1 : 0);
        boolean inputAttack = inputs.inputAttack;

        entities.forEach(entity -> {
            val input = world.getEntityManager().getComponentOf(entity, CharacterInput.class).get();
            val abilities = world.getEntityManager().getComponentOf(entity, CharacterAbilities.class).get();
            input.move.set(inputHorizontal,
                           inputVertical);
            input.attack = inputAttack;
            abilities.attackTarget.set(cursorPosition);
        });
    }
}
