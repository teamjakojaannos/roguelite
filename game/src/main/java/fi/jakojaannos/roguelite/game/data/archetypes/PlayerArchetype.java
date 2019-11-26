package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.Entities;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.NonNull;
import lombok.val;

public class PlayerArchetype {
    @NonNull
    public static Entity create(
            @NonNull final Entities entities,
            @NonNull final Transform transform
    ) {
        val player = entities.createEntity();
        entities.addComponentTo(player, transform);
        entities.addComponentTo(player, new Physics(transform));
        entities.addComponentTo(player, new Velocity());
        entities.addComponentTo(player, new CharacterInput());
        entities.addComponentTo(player, new CharacterAbilities());
        entities.addComponentTo(player, new Collider());
        entities.addComponentTo(player, new PlayerTag());
        entities.addComponentTo(player, createCharacterStats());
        entities.addComponentTo(player, createWeaponStats());
        entities.addComponentTo(player, createSpriteInfo());
        return player;
    }

    private static CharacterStats createCharacterStats() {
        return new CharacterStats(
                10.0f,
                100.0f,
                150.0f
        );
    }

    private static BasicWeaponStats createWeaponStats() {
        return new BasicWeaponStats(
                20.0,
                40.0,
                2.5
        );
    }

    private static SpriteInfo createSpriteInfo() {
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/player";

        return sprite;
    }
}
