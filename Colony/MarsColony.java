package Colony;

import Core.Agent;
import Core.Facility;
import Core.Location;
import Core.Skill;
import Emergencies.ColonyServices;
import Environment.Environment;
import Facilities.StorageFacility;
import Missions.Mission;
import Missions.MissionColony;
import Robots.Robot;
import Personnel.Human;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MarsColony implements ColonyServices, MissionColony {
    private final List<Human> crew = new ArrayList<Human>();
    private final List<Robot> robots = new ArrayList<Robot>();
    private final List<Facility> facilities = new ArrayList<Facility>();
    private final List<Mission> missionHistory = new ArrayList<Mission>();
    private Mission activeMission;
    private Mission submissionInProgress;
    private final Environment environment;

    public MarsColony() { this(null, null, null, null); }

    public MarsColony(List<Human> crew, List<Robot> robots, List<Facility> facilities) {
        this(crew, robots, facilities, null);
    }

    public MarsColony(List<Human> crew, List<Robot> robots, List<Facility> facilities, Environment environment) {
        if (crew != null) this.crew.addAll(crew);
        if (robots != null) this.robots.addAll(robots);
        if (facilities != null) this.facilities.addAll(facilities);
        this.environment = environment == null ? new Environment() : environment;
    }

    public List<Human> getCrew() { return Collections.unmodifiableList(crew); }
    public List<Robot> getRobots() { return Collections.unmodifiableList(robots); }
    public List<Facility> getFacilities() { return Collections.unmodifiableList(facilities); }
    public List<Mission> getMissionHistory() { return Collections.unmodifiableList(missionHistory); }
    public Mission getActiveMission() { return activeMission; }
    public Environment getEnvironment() { return environment; }
    public void addCrew(Human human) { crew.add(human); }
    public void addRobot(Robot robot) { robots.add(robot); }
    public void addFacility(Facility facility) { facilities.add(facility); }

    public int getTotalResource(String type) {
        if (type == null) return 0;
        int total = 0;
        for (Facility facility : facilities)
            if (facility.getOperationalStatus()) total += facility.amountOf(type);
        return total;
    }

    public boolean hasResources(Map<String, Integer> requirements) {
        if (requirements == null) return true;
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() < 0
                    || getTotalResource(entry.getKey()) < entry.getValue()) return false;
        }
        return true;
    }

    @Override
    public boolean consume(Map<String, Integer> requirements) {
        if (!hasResources(requirements)) return false;
        if (requirements == null) return true;
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            int remaining = entry.getValue();
            List<Facility> sources = new ArrayList<Facility>();
            for (Facility facility : facilities)
                if (facility.getOperationalStatus()) sources.add(facility);
            Collections.sort(sources, new Comparator<Facility>() {
                @Override public int compare(Facility a, Facility b) {
                    boolean as = a instanceof StorageFacility, bs = b instanceof StorageFacility;
                    return as == bs ? 0 : (as ? -1 : 1);
                }
            });
            for (Facility facility : sources) {
                int used = Math.min(facility.amountOf(entry.getKey()), remaining);
                if (used > 0 && !facility.remove(entry.getKey(), used)) return false;
                remaining -= used;
                if (remaining == 0) break;
            }
        }
        return true;
    }

    @Override
    public List<Agent> findAvailableAgents(Skill skill) {
        List<Agent> available = new ArrayList<Agent>();
        for (Human human : crew)
            if (human.getAvailability() && human.canPerform(skill)) available.add(human);
        for (Robot robot : robots)
            if (robot.getAvailability() && robot.canPerform(skill)) available.add(robot);
        return available;
    }

    @Override
    public List<Agent> agentsAt(Location location) {
        List<Agent> found = new ArrayList<Agent>();
        if (location == null) return found;
        for (Agent agent : allAgents())
            if (agent.getLocation() != null
                    && Objects.equals(agent.getLocation().getLocation(), location.getLocation())) found.add(agent);
        return found;
    }

    @Override
    public void failActiveMissionInvolving(Agent agent, String reason) {
        if (activeMission != null && activeMission.getAssignedAgents().contains(agent)) {
            activeMission.fail(reason);
            activeMission = null;
        }
    }

    private List<Agent> allAgents() {
        List<Agent> agents = new ArrayList<Agent>();
        agents.addAll(crew);
        agents.addAll(robots);
        return agents;
    }

    public boolean submitMission(Mission mission) {
        if (mission == null) throw new IllegalArgumentException("Mission cannot be null");
        if (mission.getState() != Missions.MissionState.PENDING) return false;
        if (activeMission != null) {
            mission.fail("Another mission is currently active");
            missionHistory.add(mission);
            return false;
        }
        for (Agent agent : mission.getAssignedAgents()) {
            if (agent == null || !allAgents().contains(agent) || !agent.getAvailability()) {
                mission.fail("Assigned agent is not available in this colony");
                missionHistory.add(mission);
                return false;
            }
        }
        if (environment.isCommDisruption()) {
            for (Agent agent : mission.getAssignedAgents()) {
                if (agent.requiresRemoteControl()) {
                    mission.fail("Communication disruption prevents remote drone operations");
                    missionHistory.add(mission);
                    return false;
                }
            }
        }
        if (!mission.validateRequirements(this, environment)) {
            mission.fail(mission.getFailureReason());
            missionHistory.add(mission);
            return false;
        }
        if (!consume(mission.getRequiredResources())) {
            mission.fail("Insufficient colony-wide operational resources");
            missionHistory.add(mission);
            return false;
        }
        submissionInProgress = mission;
        try {
            mission.markAssigned(this);
        } finally {
            submissionInProgress = null;
        }
        activeMission = mission;
        missionHistory.add(mission);
        return true;
    }

    public void clearActiveMission(Mission mission) {
        if (activeMission == mission && (mission.getState() == Missions.MissionState.COMPLETED
                || mission.getState() == Missions.MissionState.FAILED)) activeMission = null;
    }

    public boolean isAcceptingMission(Mission mission) { return submissionInProgress == mission; }

    public String generateStatusReport() {
        return new Reporting.StatusReport().generate(this);
    }

}
