package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class BasicWeaponStats implements Component {

    public double attackRate = 2.0; // Attacks per second
    public double attackProjectileSpeed = 40.0;
    public double attackSpread = 2.5;

}
