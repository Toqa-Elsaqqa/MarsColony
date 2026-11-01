package Missions;

import Core.Agent;
import Core.Equipment;
import Core.EquipmentType;
import Core.Location;
import Core.MartianSurface;
import Core.Skill;
import Environment.Environment;
import Colony.MarsColony;
import Core.Facility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Mission {
    private final String id;
    private final String name;
    private final int duration;
    private int timeElapsed;
    private MissionState state = MissionState.PENDING;
    private String failureReason;
    private final List<Agent> assignedAgents;
    private final Map<String, Integer> requiredResources;
    private Facility targetFacility;
    private Location targetLocation;
    private final List<Equipment> equipment = new ArrayList<Equipment>();

    protected Mission(String id, String name, int duration, List<Agent> assignedAgents,
                      Map<String, Integer> requiredResources) {
        if (duration < 1) throw new IllegalArgumentException("Mission duration must be positive");
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.assignedAgents = assignedAgents == null ? new ArrayList<Agent>() : new ArrayList<Agent>(assignedAgents);
        this.requiredResources = requiredResources == null
                ? new LinkedHashMap<String, Integer>() : new LinkedHashMap<String, Integer>(requiredResources);
    }

    protected Mission(String id, String name, int duration, List<Agent> assignedAgents) {
        this(id, name, duration, assignedAgents, null);
    }

    protected Mission(String id, String name, int duration) {
        this(id, name, duration, null, null);
    }

    public abstract boolean validateRequirements(MarsColony colony, Environment env);

    public String getId() { return id; }
    public String getName() { return name; }
    public int getDuration() { return duration; }
    public int getTimeElapsed() { return timeElapsed; }
    public MissionState getState() { return state; }
    public String getFailureReason() { return failureReason; }
    public List<Agent> getAssignedAgents() { return Collections.unmodifiableList(assignedAgents); }
    public Map<String, Integer> getRequiredResources() { return Collections.unmodifiableMap(requiredResources); }
    public Facility getTargetFacility() { return targetFacility; }
    public void setTargetFacility(Facility targetFacility) { this.targetFacility = targetFacility; }
    public Location getTargetLocation() { return targetLocation; }
    public void setTargetLocation(Location targetLocation) { this.targetLocation = targetLocation; }
    public void addEquipment(Equipment item) {
        if (item == null) throw new IllegalArgumentException("Equipment cannot be null");
        equipment.add(item);
    }
    public List<Equipment> getEquipment() { return Collections.unmodifiableList(equipment); }
    protected void setFailureReason(String reason) { this.failureReason = reason; }

    public boolean agentsCapable() {
        return true;
    }

    public final void markAssigned(Colony.MarsColony colony) {
        if (colony == null || colony.getActiveMission() != null || !colony.isAcceptingMission(this))
            throw new IllegalStateException("Mission acceptance requires an idle colony");
        if (state != MissionState.PENDING) throw new IllegalStateException("Mission is not pending");
        state = MissionState.ASSIGNED;
        for (Agent agent : assignedAgents) agent.setAvailability(false);
    }

    private void start() {
        if (state != MissionState.ASSIGNED) return;
        state = MissionState.IN_PROGRESS;
        Location destination = getMissionLocation();
        if (destination != null)
            for (Agent agent : assignedAgents) agent.setLocation(destination);
    }

    /** Advances the mission through its controlled simulation entry point. */
    public final void advanceOneTimeUnit(MarsColony colony, Environment env) {
        if (state == MissionState.ASSIGNED) start();
        step(colony, env);
    }

    private void step(MarsColony colony, Environment env) {
        if (state != MissionState.IN_PROGRESS) return;
        if (env.isCommDisruption() && requiresRemoteControl()) {
            fail("Communication disruption interrupted remote drone mission");
            return;
        }
        if (env.isDustStorm() && hasSurfaceRover()) {
            fail("Dust storm interrupted surface rover mission");
            return;
        }
        String hazard = environmentalFailure(env);
        if (hazard != null) { fail(hazard); return; }
        if (agentsDepleted()) { fail("Assigned agent battery depleted"); return; }
        if (equipmentFailed()) { fail("Mission equipment failure"); return; }
        timeElapsed++;
        for (Agent agent : assignedAgents) agent.onMissionTick();
        if (timeElapsed >= duration) complete(colony);
    }

    protected String environmentalFailure(Environment env) { return null; }
    protected boolean hasEquipmentFailure() { return false; }
    protected void applyCompletion(MarsColony colony) { }

    private void complete(MarsColony colony) {
        if (state != MissionState.IN_PROGRESS) return;
        applyCompletion(colony);
        state = MissionState.COMPLETED;
        releaseAgents();
    }

    public void fail(String reason) {
        if (state == MissionState.COMPLETED || state == MissionState.FAILED) return;
        state = MissionState.FAILED;
        failureReason = reason == null || reason.trim().isEmpty() ? "Mission failed" : reason;
        releaseAgents();
    }

    private void releaseAgents() {
        for (Agent agent : assignedAgents) agent.setAvailability(true);
    }

    private boolean agentsDepleted() {
        for (Agent agent : assignedAgents) if (!agent.canContinueMission()) return true;
        return false;
    }

    private boolean equipmentFailed() {
        if (hasEquipmentFailure()) return true;
        for (Equipment item : equipment) if (item.isBroken()) return true;
        for (Agent agent : assignedAgents)
            for (Equipment item : agent.getEquipment()) if (item.isBroken()) return true;
        return false;
    }

    protected final void requireResource(String type, int amount) {
        Integer current = requiredResources.get(type);
        if (current == null || current < amount) requiredResources.put(type, amount);
    }

    protected boolean hasAgentWithSkill(Skill skill) {
        for (Agent agent : assignedAgents) if (agent.canPerform(skill)) return true;
        return false;
    }

    protected boolean hasHumanWithSkill(Skill skill) {
        for (Agent agent : assignedAgents) if (agent.isHuman() && agent.canPerform(skill)) return true;
        return false;
    }

    protected boolean hasRobotWithSkill(Skill skill) {
        for (Agent agent : assignedAgents) if (!agent.isHuman() && agent.canPerform(skill)) return true;
        return false;
    }

    protected boolean hasSurfaceRover() {
        for (Agent agent : assignedAgents) if (agent.isSurfaceRover()) return true;
        return false;
    }

    protected boolean requiresRemoteControl() {
        for (Agent agent : assignedAgents) if (agent.requiresRemoteControl()) return true;
        return false;
    }

    protected boolean hasOperationalRequiredEquipment() {
        for (Equipment item : equipment) if (!item.isBroken()
                && (item.getType() == EquipmentType.EXPLORATION || item.getType() == EquipmentType.SCIENTIFIC_INSTRUMENT)) return true;
        for (Agent agent : assignedAgents)
            if (agent.hasOperationalEquipment(EquipmentType.EXPLORATION, EquipmentType.SCIENTIFIC_INSTRUMENT)) return true;
        return false;
    }

    protected Location getMissionLocation() {
        if (targetLocation != null) return targetLocation;
        if (targetFacility != null) return targetFacility;
        return hasSurfaceRover() ? MartianSurface.INSTANCE : null;
    }
}
