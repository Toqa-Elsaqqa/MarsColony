package Emergencies;

import Core.Agent;
import Core.Location;
import Core.Skill;
import java.util.List;
import java.util.Map;

// Everything the emergency handler needs from the colony (implemented by Person B)
public interface ColonyServices {
    List<Agent> findAvailableAgents(Skill skill);
    List<Agent> agentsAt(Location location);
    boolean consume(Map<String, Integer> required);

    int getTotalResource(String type);

    // If the active mission involves this agent, it becomes FAILED with the given
    // reason and all its agents are released (does nothing if there is no such mission)
    void failActiveMissionInvolving(Agent agent, String reason);
}
