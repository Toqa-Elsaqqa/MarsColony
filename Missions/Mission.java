package Missions;

import Core.Agent;
<<<<<<< Updated upstream
=======
import Core.Equipment;
import Core.EquipmentType;
>>>>>>> Stashed changes
import Core.Location;
import Core.MartianSurface;
import Core.Skill;
import java.lang.reflect.Method;
import Environment.Environment;
import Colony.MarsColony;
import Core.Facility;
<<<<<<< Updated upstream
import Personnel.Human;
import Robots.Robot;
=======
>>>>>>> Stashed changes

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
<<<<<<< Updated upstream
=======
    private boolean agentsMarkedUnavailable;
    private final List<Equipment> equipment = new ArrayList<Equipment>();
>>>>>>> Stashed changes

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
<<<<<<< Updated upstream
=======
        if (env.isCommDisruption() && requiresRemoteControl()) {
            fail("Communication disruption interrupted remote drone mission");
            return;
        }
        if (env.isDustStorm() && hasSurfaceRover()) {
            fail("Dust storm interrupted surface rover mission");
            return;
        }
>>>>>>> Stashed changes
        String hazard = environmentalFailure(env);
        if (hazard != null) { fail(hazard); return; }
        if (agentsDepleted()) { fail("Assigned agent battery depleted"); return; }
        if (equipmentFailed()) { fail("Mission equipment failure"); return; }
        timeElapsed++;
<<<<<<< Updated upstream
        for (Agent agent : assignedAgents) {
            changeNumeric(agent, "getBattery", "setBattery", -1);
            changeNumeric(agent, "getFatigue", "setFatigue", 1);
        }
=======
        for (Agent agent : assignedAgents) agent.onMissionTick();
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
        for (Agent agent : assignedAgents) {
            Integer battery = number(agent, "getBattery");
            if (battery != null && battery <= 0) return true;
        }
=======
        for (Agent agent : assignedAgents) if (!agent.canContinueMission()) return true;
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    protected static Object invoke(Object target, String method) {
        if (target == null) return null;
        try { return target.getClass().getMethod(method).invoke(target); }
        catch (Exception ignored) { return null; }
    }

    protected static Boolean bool(Object target, String method) {
        Object value = invoke(target, method);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    protected static Integer number(Object target, String method) {
        Object value = invoke(target, method);
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    private static void changeNumeric(Object target, String getter, String setter, int delta) {
        Integer current = number(target, getter);
        if (current == null) return;
        try { target.getClass().getMethod(setter, int.class).invoke(target, Math.max(0, current + delta)); }
        catch (Exception ignored) { }
=======
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
>>>>>>> Stashed changes
    }
}
