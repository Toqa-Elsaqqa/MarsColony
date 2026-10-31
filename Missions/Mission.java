package Missions;

import Core.Agent;
import Core.Location;
import Core.Skill;
import java.lang.reflect.Method;
import Environment.Environment;
import Colony.MarsColony;
import Core.Facility;
import Personnel.Human;
import Robots.Robot;

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
    private MissionState state = MissionState.ASSIGNED;
    private String failureReason;
    private final List<Agent> assignedAgents;
    private final Map<String, Integer> requiredResources;
    private Facility targetFacility;
    private Location targetLocation;

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
    protected void setFailureReason(String reason) { this.failureReason = reason; }

    public boolean agentsCapable() {
        return true;
    }

    public void markAssigned() {
        if (state != MissionState.ASSIGNED) return;
        for (Agent agent : assignedAgents) agent.setAvailability(false);
    }

    public void start() {
        if (state == MissionState.ASSIGNED) state = MissionState.IN_PROGRESS;
    }

    public void step(MarsColony colony, Environment env) {
        if (state != MissionState.IN_PROGRESS) return;
        String hazard = environmentalFailure(env);
        if (hazard != null) { fail(hazard); return; }
        if (agentsDepleted()) { fail("Assigned agent battery depleted"); return; }
        if (equipmentFailed()) { fail("Mission equipment failure"); return; }
        timeElapsed++;
        for (Agent agent : assignedAgents) {
            changeNumeric(agent, "getBattery", "setBattery", -1);
            changeNumeric(agent, "getFatigue", "setFatigue", 1);
        }
        if (timeElapsed >= duration) complete(colony);
    }

    protected String environmentalFailure(Environment env) { return null; }
    protected boolean hasEquipmentFailure() { return false; }
    protected void applyCompletion(MarsColony colony) { }

    public void complete(MarsColony colony) {
        if (state != MissionState.IN_PROGRESS && state != MissionState.ASSIGNED) return;
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
        for (Agent agent : assignedAgents) {
            Integer battery = number(agent, "getBattery");
            if (battery != null && battery <= 0) return true;
        }
        return false;
    }

    private boolean equipmentFailed() {
        if (hasEquipmentFailure()) return true;
        for (Agent agent : assignedAgents) {
            if (Boolean.FALSE.equals(bool(agent, "isOperational"))) return true;
            for (String getter : new String[] {"getEquipment", "getAssignedEquipment"}) {
                Object equipment = invoke(agent, getter);
                if (equipment instanceof Iterable<?>) {
                    for (Object item : (Iterable<?>) equipment) {
                        if (Boolean.FALSE.equals(bool(item, "isOperational"))) return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean hasAgent(Class<?> type) {
        for (Agent agent : assignedAgents) if (type.isInstance(agent)) return true;
        return false;
    }

    protected boolean hasNamedAgent(String simpleName) {
        for (Agent agent : assignedAgents) if (agent.getClass().getSimpleName().equals(simpleName)) return true;
        return false;
    }

    protected boolean agentHasSkill(String simpleName, String skillName) {
        for (Agent agent : assignedAgents) {
            if (!agent.getClass().getSimpleName().equals(simpleName)) continue;
            try {
            Skill skill = Skill.valueOf(skillName);
            if (agent.canPerform(skill)) return true;
            } catch (Exception ignored) { }
        }
        return false;
    }

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
    }
}
