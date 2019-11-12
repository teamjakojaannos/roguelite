package fi.jakojäännös.roguelite.engine.utilities;

public interface TimeManager {
    long getCurrentRealTime();

    long getCurrentGameTime();

    void progressGameTime(long timestep);

    void refresh();
}
