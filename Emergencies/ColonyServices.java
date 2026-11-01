package Emergencies;

import Core.Agent;
import Core.Location;
import Core.Skill;
import java.util.List;
import java.util.Map;

// Everything the emergency handler needs from the colony (implemented by Person B)
public interface ColonyServices {

    // Agents that are available and able to perform the skill
    List<Agent> findAvailableAgents(Skill skill);

    // All agents currently located at the given location
    List<Agent> agentsAt(Location location);

    // All-or-nothing: consumes every resource or none of them
    boolean consume(Map<String, Integer> required);

    // If the active mission involves this agent, it becomes FAILED with the given
    // reason and all its agents are released (does nothing if there is no such mission)
    void failActiveMissionInvolving(Agent agent, String reason);
}
