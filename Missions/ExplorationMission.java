package Missions;

import Core.Agent;
import Environment.Environment;
import Environment.WeatherCondition;
import Colony.MarsColony;
import Core.EquipmentType;
import Core.Skill;
import java.util.List;
import java.util.Map;

public class ExplorationMission extends Mission {
    public ExplorationMission(String id, String name, int duration, List<Agent> agents, Map<String, Integer> resources) {
        super(id, name, duration, agents, resources);
    }

    public ExplorationMission(String id, String name, int duration, List<Agent> agents) {
        super(id, name, duration, agents);
    }

    @Override public boolean validateRequirements(MarsColony colony, Environment env) {
        if (env.isDustStorm()) return reject("Dust storm prevents exploration");
        if (!hasSurfaceRover() || !hasAgentWithSkill(Skill.EXPLORATION))
            return reject("Exploration rover with exploration skill required");
        if (!hasAgentWithSkill(Skill.RESEARCH)) return reject("Scientist with research skill required");
        if (!hasOperationalRequiredEquipment()) return reject("Operational exploration or scientific equipment required");
        for (Agent agent : getAssignedAgents())
            if (agent.isSurfaceRover() && !agent.canContinueMission()) return reject("Rover battery depleted");
        return true;
    }

    private boolean reject(String reason) { setFailureReason(reason); return false; }
}
