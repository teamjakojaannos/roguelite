package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.archetypes.BasicProjectile;
import fi.jakojaannos.roguelite.game.data.components.BasicWeaponStats;
import fi.jakojaannos.roguelite.game.data.components.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.NonNull;
import lombok.val;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class CharacterAttackSystem implements ECSSystem {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            Transform.class, CharacterInput.class, CharacterAbilities.class, BasicWeaponStats.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    private final Vector2d tmpSpreadOffset = new Vector2d();
    private final Random random = new Random(1337);

    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        val cluster = world.getEntities();
        entities.forEach(entity -> {
            val input = cluster.getComponentOf(entity, CharacterInput.class).get();
            val abilities = cluster.getComponentOf(entity, CharacterAbilities.class).get();
            val weapon = cluster.getComponentOf(entity, BasicWeaponStats.class).get();

            if (input.attack && abilities.attackTimer >= 1.0 / weapon.attackRate) {
                val character = cluster.getComponentOf(entity, Transform.class).get();

                val projectileX = character.getCenterX();
                val projectileY = character.getCenterY();

                val direction = new Vector2d(abilities.attackTarget)
                        .sub(projectileX, projectileY)
                        .normalize();
                tmpSpreadOffset.set(direction)
                        .perpendicular()
                        .mul((random.nextDouble() * 2.0 - 1.0) * weapon.attackSpread);

                BasicProjectile.create(world, projectileX, projectileY, direction, weapon.attackProjectileSpeed, tmpSpreadOffset);


                abilities.attackTimer = 0.0;
            }
            abilities.attackTimer += delta;
        });
    }
}
