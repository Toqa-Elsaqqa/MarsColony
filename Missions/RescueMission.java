package Missions;

import Core.Agent;
import Core.Facility;
import Environment.Environment;
import Environment.WeatherCondition;
import Colony.MarsColony;
import java.util.List;
import java.util.Map;

public class RescueMission extends Mission {
    private Agent strandedAgent;

    public RescueMission(String id, String name, int duration, List<Agent> agents, Map<String, Integer> resources, Agent strandedAgent) {
        super(id, name, duration, agents, resources);
        this.strandedAgent = strandedAgent;
    }

    public RescueMission(String id, String name, int duration, List<Agent> agents, Map<String, Integer> resources) {
        super(id, name, duration, agents, resources);
    }

    public void setStrandedAgent(Agent strandedAgent) { this.strandedAgent = strandedAgent; }
    public Agent getStrandedAgent() { return strandedAgent; }

    @Override public boolean validateRequirements(MarsColony colony, Environment env) {
        if (env.isCommDisruption()) return reject("Communication disruption prevents rescue coordination");
        boolean station = false;
        for (Facility facility : colony.getFacilities())
            if (facility.getOperationalStatus() && facility.getClass().getSimpleName().equals("CommunicationStation")) station = true;
        if (!station) return reject("Operational CommunicationStation required");
        boolean vehicle = false;
        for (Agent agent : getAssignedAgents())
            if (agent instanceof Robots.Robot && (agent.getClass().getSimpleName().contains("Rover") || agent.getClass().getSimpleName().contains("Drone"))) vehicle = true;
        if (!vehicle) return reject("Suitable rescue vehicle required");
        if (getAssignedAgents().isEmpty()) return reject("Rescue personnel required");
        return true;
    }

    @Override protected String environmentalFailure(Environment env) {
        return env.getWeatherCondition() == WeatherCondition.COMMUNICATION_DISRUPTION
                ? "Communication disruption interrupted rescue" : null;
    }

    @Override protected void applyCompletion(MarsColony colony) {
        if (strandedAgent != null) strandedAgent.setAvailability(true);
    }

    private boolean reject(String reason) { setFailureReason(reason); return false; }
}
