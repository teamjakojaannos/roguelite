package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.game.data.DamageInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class Health implements Component {

    public List<DamageInstance> damageInstances = new ArrayList<>();

    public double maxHealth = 100.0,
    /**
     * Use addDamageInstance(...) to modify this!
     */
    currentHealth = 100.0;

    public Health(double maxHp, double currentHp) {
        this.maxHealth = maxHp;
        this.currentHealth = currentHp;
    }

    public Health(double maxHp) {
        this.maxHealth = maxHp;
        this.currentHealth = maxHp;
    }

    public void addDamageInstance(DamageInstance dmg) {
        damageInstances.add(dmg);
    }


}
