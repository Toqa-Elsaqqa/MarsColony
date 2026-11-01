package Missions;

import Core.Agent;
import Core.Facility;
import Core.Equipment;
import Environment.Environment;
import Core.Skill;
import java.util.List;
import java.util.Map;

public class RepairMission extends Mission {
    private Equipment targetEquipment;

    public RepairMission(String id, String name, int duration, List<Agent> agents, Map<String, Integer> resources, Facility target) {
        super(id, name, duration, agents, resources);
        setTargetFacility(target);
        requireResource("SPARE_PARTS", 1);
    }

    public RepairMission(String id, String name, int duration, List<Agent> agents, Map<String, Integer> resources) {
        super(id, name, duration, agents, resources);
        requireResource("SPARE_PARTS", 1);
    }

    public void setTargetEquipment(Equipment targetEquipment) { this.targetEquipment = targetEquipment; }
    public Equipment getTargetEquipment() { return targetEquipment; }

    @Override public boolean validateRequirements(MissionColony colony, Environment env) {
        if (getTargetFacility() == null && targetEquipment == null) return reject("Repair target required");
        if (!hasHumanWithSkill(Skill.REPAIR)) return reject("Engineer with repair skill required");
        if (!hasRobotWithSkill(Skill.REPAIR))
            return reject("Repair robot with repair skill required");
        return true;
    }

    @Override protected void applyCompletion(MissionColony colony) {
        if (targetEquipment != null) targetEquipment.repair();
        if (getTargetFacility() != null) {
            getTargetFacility().repairEquipment();
            getTargetFacility().setOperationalStatus(true);
        }
    }

    private boolean reject(String reason) { setFailureReason(reason); return false; }

    @Override protected Core.Location getMissionLocation() {
        return getTargetFacility() == null ? super.getMissionLocation() : getTargetFacility();
    }
}
