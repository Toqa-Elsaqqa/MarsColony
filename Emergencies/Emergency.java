package Emergencies;

import Core.Location;
import Core.Skill;
import java.util.List;
import java.util.Map;

public abstract class Emergency {
    private EmergencyState state = EmergencyState.ACTIVE;
    private String failureReason;
    private boolean detected = false;
    private final Location location;

    protected Emergency(Location location) {
        if (location == null)
            throw new IllegalArgumentException("Invalid Location");
        this.location = location;
    }

    // What this emergency needs from responders and from the colony
    public abstract List<Skill> requiredHumanSkills();
    public abstract List<Skill> requiredRobotSkills();
    public abstract Map<String, Integer> responseCost();

    // Immediate effects when the emergency is detected
    protected abstract void applyDetectionEffects(ColonyServices colony);
    // Extra damage when the emergency ends UNRESOLVED
    protected abstract void applyUnresolvedEffects(ColonyServices colony);

    // Optional hooks (default: do nothing)
    public String preconditionFailure() { return null; }          // reason, or null if OK
    protected void applyResolutionEffects(ColonyServices colony) { }
    public List<Emergency> followUps() { return List.of(); }      // emergencies triggered by this one

    public final void detect(ColonyServices colony) {
        if (detected)
            throw new IllegalStateException("Emergency already detected");
        detected = true;
        applyDetectionEffects(colony);
    }

    public final void resolve(ColonyServices colony) {
        requireActive();
        applyResolutionEffects(colony);
        state = EmergencyState.RESOLVED;
    }

    public final void fail(String reason, ColonyServices colony) {
        requireActive();
        applyUnresolvedEffects(colony);
        state = EmergencyState.UNRESOLVED;
        failureReason = reason;
    }

    // A closed emergency can never be reopened (no automatic retry)
    private void requireActive() {
        if (state != EmergencyState.ACTIVE)
            throw new IllegalStateException("Emergency already closed");
    }

    public Location getLocation() { return location; }
    public EmergencyState getState() { return state; }
    public String getFailureReason() { return failureReason; }
}
