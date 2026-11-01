package Missions;

import Core.Facility;
import java.util.List;

/** Colony services used by mission lifecycle and requirement checks. */
public interface MissionColony {
    Mission getActiveMission();
    boolean isAcceptingMission(Mission mission);
    List<Facility> getFacilities();
}
