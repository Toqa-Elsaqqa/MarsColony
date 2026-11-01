package Missions;

import Core.Agent;
import Core.Facility;
import Core.EquipmentType;
import Environment.Environment;
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

    @Override public boolean validateRequirements(MissionColony colony, Environment env) {
        Facility lab = getTargetFacility();
        if (lab == null || !lab.getOperationalStatus() || !lab.supportsResearch())
            return reject("An operational research laboratory is required");
        if (!lab.hasOperationalEquipment(EquipmentType.SCIENTIFIC_INSTRUMENT))
            return reject("Operational scientific equipment in the laboratory is required");
        if (!hasAgentWithSkill(Core.Skill.RESEARCH)) return reject("Scientist with research skill required");
        return true;
    }

    @Override protected boolean hasEquipmentFailure() {
        Facility lab = getTargetFacility();
        return lab == null || !lab.hasOperationalEquipment(EquipmentType.SCIENTIFIC_INSTRUMENT);
    }

    private boolean reject(String reason) { setFailureReason(reason); return false; }
}
