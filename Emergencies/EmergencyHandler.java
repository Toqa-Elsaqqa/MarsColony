package Emergencies;

import Core.Agent;
import Core.Skill;
import java.util.ArrayList;
import java.util.List;

public class EmergencyHandler {
    private final ColonyServices colony;

    public EmergencyHandler(ColonyServices colony) { this.colony = colony; }

    public void handle(Emergency e) {
        process(e);
        // Run emergencies triggered by this one (e.g. injuries after a fire)
        for (Emergency follow : e.followUps())
            handle(follow);
    }

    private void process(Emergency e) {
        // 1. Detect: apply immediate effects
        e.detect(colony);

        // 2. Stop early if a precondition fails (e.g. facility not operational)
        String blocked = e.preconditionFailure();
        if (blocked != null) {
            e.fail(blocked, colony);
            return;
        }

        // 3. Pick one distinct responder per required skill
        List<Agent> chosen = new ArrayList<>();
        String missing = pick(e.requiredHumanSkills(), true, chosen);
        if (missing == null)
            missing = pick(e.requiredRobotSkills(), false, chosen);
        if (missing != null) {
            e.fail("No available " + missing, colony);   // nothing consumed
            return;
        }

        // 4. Consume response resources (all-or-nothing)
        if (!colony.consume(e.responseCost())) {
            e.fail("Insufficient resources for the response", colony);
            return;                                      // nobody was dispatched
        }

        // 5. Dispatch responders to the affected location
        for (Agent a : chosen) {
            a.setLocation(e.getLocation());
            a.setAvailability(false);
        }

        // 6. Response succeeded: close the emergency and release responders
        e.resolve(colony);
        for (Agent a : chosen)
            a.setAvailability(true);
    }

    // Returns a description of the first missing responder, or null if all were found
    private String pick(List<Skill> skills, boolean human, List<Agent> chosen) {
        for (Skill skill : skills) {
            Agent found = null;
            for (Agent a : colony.findAvailableAgents(skill)) {
                if (a.isHuman() == human && !chosen.contains(a)) {
                    found = a;
                    break;
                }
            }
            if (found == null)
                return (human ? "human" : "robotic") + " responder with skill " + skill;
            chosen.add(found);
        }
        return null;
    }
}
