package Missions;

import Core.Agent;
import Core.Facility;
import Environment.Environment;
import Colony.MarsColony;
import Personnel.Scientist;
import java.util.List;
import java.util.Map;

public class ResearchMission extends Mission {
    public ResearchMission(String id, String name, int duration, List<Agent> agents, Map<String, Integer> resources, Facility lab) {
        super(id, name, duration, agents, resources);
        setTargetFacility(lab);
    }

    public ResearchMission(String id, String name, int duration, List<Agent> agents, Map<String, Integer> resources) {
        super(id, name, duration, agents, resources);
    }

    @Override public boolean validateRequirements(MarsColony colony, Environment env) {
        Facility lab = getTargetFacility();
        if (lab == null || !lab.getOperationalStatus() || !lab.getClass().getSimpleName().equals("Lab"))
            return reject("An operational Lab is required");
        if (!hasAgent(Scientist.class) || !agentHasSkill("Scientist", "RESEARCH")) return reject("Scientist required");
        return true;
    }

    private boolean reject(String reason) { setFailureReason(reason); return false; }
}
