package Missions;

import Core.Agent;
import Core.Facility;
import Core.Equipment;
import Environment.Environment;
import Colony.MarsColony;
import Personnel.Engineer;
import Robots.RepairRobot;
import java.util.List;
import java.util.Map;

public class RepairMission extends Mission {
    private Object targetEquipment;

    public RepairMission(String id, String name, int duration, List<Agent> agents, Map<String, Integer> resources, Facility target) {
        super(id, name, duration, agents, resources);
        setTargetFacility(target);
    }

    public RepairMission(String id, String name, int duration, List<Agent> agents, Map<String, Integer> resources) {
        super(id, name, duration, agents, resources);
    }

    public void setTargetEquipment(Object targetEquipment) { this.targetEquipment = targetEquipment; }
    public Object getTargetEquipment() { return targetEquipment; }

    @Override public boolean validateRequirements(MarsColony colony, Environment env) {
        if (getTargetFacility() == null && targetEquipment == null) return reject("Repair target required");
        if (!hasAgent(Engineer.class) || !agentHasSkill("Engineer", "REPAIR")) return reject("Engineer with repair skill required");
        if (!hasAgent(RepairRobot.class) || !agentHasSkill("RepairRobot", "REPAIR"))
            return reject("Repair robot with repair skill required");
        return true;
    }

    @Override protected void applyCompletion(MarsColony colony) {
        Object target = getTargetFacility() == null ? targetEquipment : getTargetFacility();
        if (target instanceof Equipment) ((Equipment) target).repair();
        else if (target instanceof Facility) ((Facility) target).setOperationalStatus(true);
    }

    private boolean reject(String reason) { setFailureReason(reason); return false; }
}
