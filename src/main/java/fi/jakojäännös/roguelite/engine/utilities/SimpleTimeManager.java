package fi.jakojäännös.roguelite.engine.utilities;

public class SimpleTimeManager implements TimeManager {
    private long currentTime;

    @Override
    public long getCurrentTime() {
        return currentTime;
    }

    public void tick() {
        this.currentTime = System.currentTimeMillis();
    }
}
