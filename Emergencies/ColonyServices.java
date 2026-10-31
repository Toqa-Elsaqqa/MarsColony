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
    void failActiveMissionInvolving(Agent agent, String reason);
}
