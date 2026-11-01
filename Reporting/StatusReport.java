package Reporting;

import Colony.MarsColony;
import Core.Agent;
import Core.Facility;
import Emergencies.Emergency;
import Emergencies.EmergencyHandler;
import Missions.Mission;
import Personnel.Human;
import Robots.Robot;

// Builds the colony status report from the colony's public API only.
public class StatusReport {
    // Resource types named in the specification (Section 5)
    public static final String[] RESOURCES = {
        "OXYGEN", "WATER", "FOOD", "FUEL", "BATTERY_POWER", "MEDICAL_SUPPLIES", "SPARE_PARTS"
    };

    public StatusReport() { }

    public String generate(MarsColony colony) { return generate(colony, null); }

    public static String generate(MarsColony colony, EmergencyHandler handler) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MARS COLONY STATUS REPORT ===\n");
        sb.append("Environment: ").append(colony.getEnvironment().getWeatherCondition()).append('\n');

        sb.append("\nColony-wide resources (operational facilities only):\n");
        for (String type : RESOURCES)
            sb.append(String.format("  %-17s %d%n", type, colony.getTotalResource(type)));

        sb.append("\nFacilities:\n");
        for (Facility f : colony.getFacilities())
            sb.append("  ").append(f.getFacId()).append(" - ")
              .append(f.getOperationalStatus() ? "operational" : "NOT operational").append('\n');

        sb.append("\nCrew:\n");
        for (Human h : colony.getCrew())
            sb.append(String.format("  %-8s %-17s health=%-3d fatigue=%-3d %-9s at %s%n",
                    h.getName(), h.getClass().getSimpleName(), h.getHealth(), h.getFatigue(),
                    h.getAvailability() ? "available" : "busy", h.getLocation().getLocation()));

        sb.append("\nRobots:\n");
        for (Robot r : colony.getRobots())
            sb.append(String.format("  %-8s %-17s health=%-3d battery=%-3d %-9s at %s%n",
                    r.getName(), r.getClass().getSimpleName(), r.getHealth(), r.getBattery(),
                    r.getAvailability() ? "available" : "busy", r.getLocation().getLocation()));

        Mission active = colony.getActiveMission();
        sb.append("\nActive mission: ").append(active == null ? "none"
                : active.getName() + " [" + active.getState() + ", "
                  + active.getTimeElapsed() + "/" + active.getDuration() + "]").append('\n');

        sb.append("\nMission history:\n");
        if (colony.getMissionHistory().isEmpty()) sb.append("  (none)\n");
        for (Mission m : colony.getMissionHistory())
            sb.append("  ").append(m.getName()).append(": ").append(m.getState())
              .append(m.getFailureReason() == null ? "" : " - " + m.getFailureReason()).append('\n');

        sb.append("\nEmergency history:\n");
        if (handler == null || handler.getHistory().isEmpty()) sb.append("  (none)\n");
        else for (Emergency e : handler.getHistory())
            sb.append("  ").append(e.getClass().getSimpleName()).append(" at ")
              .append(e.getLocation().getLocation()).append(": ").append(e.getState())
              .append(e.getFailureReason() == null ? "" : " - " + e.getFailureReason()).append('\n');

        return sb.toString();
    }
}
