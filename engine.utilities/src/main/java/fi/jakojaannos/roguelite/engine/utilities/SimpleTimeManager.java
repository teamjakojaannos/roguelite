package fi.jakojaannos.roguelite.engine.utilities;

public class SimpleTimeManager implements UpdateableTimeManager {
    private final long timestep;
    private final double timestepInSeconds;

    private long currentGameTime;

    public SimpleTimeManager(long timestepInMs) {
        this.timestep = timestepInMs;
        this.timestepInSeconds = 1.0 / timestepInMs;
    }

    @Override
    public long getTimeStep() {
        return this.timestep;
    }

    @Override
    public double getTimeStepInSeconds() {
        return this.timestepInSeconds;
    }

    @Override
    public long getCurrentGameTime() {
        return this.currentGameTime;
    }

    @Override
    public void refresh() {
        this.currentGameTime += this.timestep;
    }
}
