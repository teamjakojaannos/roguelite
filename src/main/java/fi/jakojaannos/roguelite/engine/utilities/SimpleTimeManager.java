package fi.jakojaannos.roguelite.engine.utilities;

public class SimpleTimeManager implements TimeManager {
    private long currentTime;
    private long currentGameTime;

    @Override
    public long getCurrentRealTime() {
        return this.currentTime;
    }

    @Override
    public long getCurrentGameTime() {
        return this.currentGameTime;
    }

    @Override
    public void progressGameTime(long timestep) {
        this.currentGameTime += timestep;
    }

    @Override
    public void refresh() {
        this.currentTime = System.currentTimeMillis();
    }
}
