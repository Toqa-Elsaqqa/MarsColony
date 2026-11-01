package Simulation;

import Colony.MarsColony;
import Environment.Environment;
import Missions.Mission;
import Missions.MissionState;

public class Simulation {
    private final MarsColony colony;
    private final Environment environment;
    private int currentTime;

    public Simulation(MarsColony colony) {
        if (colony == null) throw new IllegalArgumentException("Colony cannot be null");
        this.colony = colony;
        this.environment = colony.getEnvironment();
    }

    public Simulation(MarsColony colony, Environment environment) {
        if (colony == null || environment == null) throw new IllegalArgumentException("Colony and environment are required");
        this.colony = colony;
        this.environment = environment;
    }

    public MarsColony getColony() { return colony; }
    public Environment getEnvironment() { return environment; }
    public int getCurrentTime() { return currentTime; }

    public void advanceTime(int timeUnits) {
        if (timeUnits < 0) throw new IllegalArgumentException("Time units cannot be negative");
        for (int i = 0; i < timeUnits; i++) {
            currentTime++;
            Mission mission = colony.getActiveMission();
            if (mission == null) continue;
            if (mission.getState() == MissionState.ASSIGNED) mission.start();
            mission.step(colony, environment);
            if (mission.getState() == MissionState.COMPLETED || mission.getState() == MissionState.FAILED)
                colony.setActiveMission(null);
        }
    }
}
