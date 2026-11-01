package Missions;

import Core.Agent;
import Environment.Environment;
import Environment.WeatherCondition;
import Colony.MarsColony;
import Robots.ExplorationRover;
import Personnel.Scientist;
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
        if (!hasAgent(ExplorationRover.class) || !agentHasSkill("ExplorationRover", "EXPLORATION"))
            return reject("Exploration rover with exploration skill required");
        if (!hasAgent(Scientist.class) || !agentHasSkill("Scientist", "RESEARCH"))
            return reject("Scientist with exploration or science skill required");
        for (Agent agent : getAssignedAgents()) {
            if (agent instanceof ExplorationRover) {
                Integer battery = number(agent, "getBattery");
                if (battery != null && battery < getDuration()) return reject("Insufficient rover battery");
            }
        }
        return true;
    }

    @Override protected String environmentalFailure(Environment env) {
        return env.getWeatherCondition() == WeatherCondition.DUST_STORM ? "Dust storm interrupted exploration" : null;
    }

    private boolean reject(String reason) { setFailureReason(reason); return false; }
}
