package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class EnemyAI implements Component {

    public double aggroRadius, targetDistance;

    public EnemyAI(double aggroRadius, double targetDistance) {
        this.aggroRadius = aggroRadius;
        this.targetDistance = targetDistance;
    }

}
