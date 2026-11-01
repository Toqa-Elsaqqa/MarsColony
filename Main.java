import Colony.MarsColony;
import Core.Agent;
import Core.Equipment;
import Core.EquipmentType;
import Core.MartianSurface;
import Core.Resource;
import Emergencies.EmergencyHandler;
import Emergencies.Fire;
import Emergencies.OxygenLeak;
import Emergencies.RoverBreakdown;
import Environment.WeatherCondition;
import Facilities.CommunicationStation;
import Facilities.Habitat;
import Facilities.Lab;
import Facilities.PowerStation;
import Facilities.StorageFacility;
import Missions.ExplorationMission;
import Missions.Mission;
import Personnel.Engineer;
import Personnel.Firefighter;
import Personnel.MedicalOfficer;
import Personnel.Scientist;
import Reporting.StatusReport;
import Robots.Drone;
import Robots.ExplorationRover;
import Robots.RepairRobot;
import Simulation.Simulation;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

// Runs the required demonstration scenarios (Section 12) plus extra emergency scenarios.
// Usage: java Main            -> runs everything
//        java Main 1..5       -> runs one required scenario
//        java Main extra      -> runs the extra emergency scenarios
public class Main {

    private static List<Agent> agents(Agent... agents) {
        return Arrays.asList(agents);
    }

    private static Map<String, Integer> resources(String type, int amount) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        result.put(type, amount);
        return result;
    }

    // A fresh, consistent colony for each scenario
    static class World {
        MarsColony colony = new MarsColony();
        Simulation sim = new Simulation(colony);
        EmergencyHandler handler = new EmergencyHandler(colony);
        Habitat hab = new Habitat(100, true, "Habitat-A");
        StorageFacility store = new StorageFacility(100, true, "Storage-1");
        Lab lab = new Lab(60, true, "Lab-1");
        CommunicationStation comm = new CommunicationStation(20, true, "Comm-1");
        PowerStation power = new PowerStation(40, true, "Power-1");
        Scientist scientist = new Scientist("Salma", lab, true, 100, 10);
        Engineer engineer = new Engineer("Omar", store, true, 100, 10);
        MedicalOfficer medic = new MedicalOfficer("Mona", hab, true, 100, 10);
        Firefighter firefighter = new Firefighter("Karim", store, true, 100, 10);
        RepairRobot repairBot = new RepairRobot("Fixer-1", store, true, 100, 100);
        ExplorationRover rover = new ExplorationRover("Rover-1", store, true, 100, 100);
        Drone drone = new Drone("Drone-1", store, true, 100, 100);

        World() {
            hab.add(new Resource("OXYGEN", 300));
            store.add(new Resource("OXYGEN", 200));
            store.add(new Resource("WATER", 50));
            store.add(new Resource("FOOD", 80));
            store.add(new Resource("FUEL", 100));
            store.add(new Resource("MEDICAL_SUPPLIES", 10));
            store.add(new Resource("SPARE_PARTS", 10));
            power.add(new Resource("BATTERY_POWER", 40));
            rover.addEquipment(new Equipment("EQ-1", EquipmentType.SCIENTIFIC_INSTRUMENT));
            colony.addFacility(hab);
            colony.addFacility(store);
            colony.addFacility(lab);
            colony.addFacility(comm);
            colony.addFacility(power);
            colony.addCrew(scientist);
            colony.addCrew(engineer);
            colony.addCrew(medic);
            colony.addCrew(firefighter);
            colony.addRobot(repairBot);
            colony.addRobot(rover);
            colony.addRobot(drone);
        }

        void report(String title) {
            System.out.println("--- " + title + " ---");
            System.out.println(StatusReport.generate(colony, handler));
        }
    }

    static void title(String text) {
        System.out.println("\n##############################################");
        System.out.println("# " + text);
        System.out.println("##############################################");
    }

    static void outcome(Mission m) {
        System.out.println("Mission '" + m.getName() + "' state: " + m.getState()
                + (m.getFailureReason() == null ? "" : "  | Reason: " + m.getFailureReason()));
    }

    // Scenario 1: scientist + rover + equipment + exploration mission succeed
    static void scenario1() {
        title("Scenario 1: Successful Exploration");
        World w = new World();
        w.report("BEFORE");
        Mission m = new ExplorationMission("M1", "Explore Valles Region", 3,
                agents(w.scientist, w.rover), resources("FUEL", 5));
        boolean accepted = w.colony.submitMission(m);
        System.out.println("Submitted. Accepted = " + accepted + ", state = " + m.getState());
        for (int t = 1; t <= 3; t++) {
            w.sim.advanceTime(1);
            System.out.println("t=" + t + " -> " + m.getState() + " (" + m.getTimeElapsed() + "/" + m.getDuration() + ")");
        }
        outcome(m);
        w.report("AFTER");
    }

    // Scenario 2: unsuitable agent -> immediate FAILED, never active
    static void scenario2() {
        title("Scenario 2: Mission Rejection");
        World w = new World();
        Mission m = new ExplorationMission("M2", "Explore with an engineer", 3,
                agents(w.engineer), resources("FUEL", 5));
        boolean accepted = w.colony.submitMission(m);
        System.out.println("Mission cannot be assigned. Accepted = " + accepted);
        outcome(m);
        System.out.println("Active mission: " + (w.colony.getActiveMission() == null ? "none (never became active)" : "ERROR"));
        System.out.println("Engineer available again/still: " + w.engineer.getAvailability());
    }

    // Scenario 3: not enough resources -> rejected, nothing consumed, never negative
    static void scenario3() {
        title("Scenario 3: Resource Shortage");
        World w = new World();
        System.out.println("Oxygen: Habitat-A=" + w.hab.amountOf("OXYGEN") + ", Storage=" + w.store.amountOf("OXYGEN")
                + ", colony total=" + w.colony.getTotalResource("OXYGEN"));

        Mission tooBig = new ExplorationMission("M3a", "Needs 600 oxygen", 2,
                agents(w.scientist, w.rover), resources("OXYGEN", 600));
        System.out.println("Submit mission needing 600 oxygen -> accepted = " + w.colony.submitMission(tooBig));
        outcome(tooBig);
        System.out.println("Oxygen after rejection: colony total=" + w.colony.getTotalResource("OXYGEN")
                + " (unchanged, never negative)");

        Mission ok = new ExplorationMission("M3b", "Needs 450 oxygen (two sources)", 2,
                agents(w.scientist, w.rover), resources("OXYGEN", 450));
        System.out.println("Submit mission needing 450 oxygen -> accepted = " + w.colony.submitMission(ok));
        outcome(ok);
        System.out.println("Oxygen after: Habitat-A=" + w.hab.amountOf("OXYGEN") + ", Storage=" + w.store.amountOf("OXYGEN")
                + ", colony total=" + w.colony.getTotalResource("OXYGEN") + " (drawn from both facilities)");
    }

    // Scenario 4: oxygen leak - repaired, then unresolved (no responder), then insufficient resources
    static void scenario4() {
        title("Scenario 4: Emergency Handling (Oxygen Leak)");
        World w = new World();
        System.out.println("[4a] Leak with responders available");
        w.report("BEFORE");
        OxygenLeak leak = new OxygenLeak(w.hab);
        w.handler.handle(leak);
        System.out.println("Affected facility: " + w.hab.getFacId());
        System.out.println("Emergency state: " + leak.getState() + ", oxygen lost: " + leak.getTotalOxygenLost());
        System.out.println("Habitat oxygen now: " + w.hab.amountOf("OXYGEN"));
        System.out.println("Spare parts left: " + w.colony.getTotalResource("SPARE_PARTS")
                + ", battery left: " + w.colony.getTotalResource("BATTERY_POWER"));
        System.out.println("Dispatched responders: " + w.engineer.getName() + " and " + w.repairBot.getName());
        System.out.println("Engineer at: " + w.engineer.getLocation().getLocation() + ", available again: " + w.engineer.getAvailability());
        System.out.println("Repair robot at: " + w.repairBot.getLocation().getLocation() + ", available again: " + w.repairBot.getAvailability());

        System.out.println("\n[4b] Leak with NO available engineer (unresolved, double oxygen loss)");
        w.engineer.setAvailability(false);
        int sparesBefore = w.colony.getTotalResource("SPARE_PARTS");
        OxygenLeak leak2 = new OxygenLeak(w.hab);
        w.handler.handle(leak2);
        System.out.println("Emergency state: " + leak2.getState() + " | Reason: " + leak2.getFailureReason());
        System.out.println("Oxygen lost: " + leak2.getTotalOxygenLost() + " (2 x " + OxygenLeak.OXYGEN_LOSS + ")");
        System.out.println("Spare parts consumed: " + (sparesBefore - w.colony.getTotalResource("SPARE_PARTS")));
        w.engineer.setAvailability(true);

        System.out.println("\n[4c] Leak with responders but NOT enough spare parts");
        w.store.remove("SPARE_PARTS", w.store.amountOf("SPARE_PARTS"));
        OxygenLeak leak3 = new OxygenLeak(w.hab);
        w.handler.handle(leak3);
        System.out.println("Emergency state: " + leak3.getState() + " | Reason: " + leak3.getFailureReason());
        System.out.println("Habitat oxygen (never below zero): " + w.hab.amountOf("OXYGEN"));
        w.report("AFTER");
    }

    // Scenario 5: dust storm affects operations
    static void scenario5() {
        title("Scenario 5: Environmental Effect (Dust Storm)");
        World w = new World();
        w.colony.getEnvironment().setWeatherCondition(WeatherCondition.DUST_STORM);
        System.out.println("Environment: " + w.colony.getEnvironment().getWeatherCondition());
        Mission m = new ExplorationMission("M5a", "Exploration during storm", 3,
                agents(w.scientist, w.rover), resources("FUEL", 5));
        System.out.println("Accepted = " + w.colony.submitMission(m));
        System.out.println("Exploration Mission cannot start.");
        outcome(m);

        // A mission already in progress when the storm arrives
        World w2 = new World();
        Mission running = new ExplorationMission("M5b", "Exploration in progress", 5,
                agents(w2.scientist, w2.rover), resources("FUEL", 5));
        w2.colony.submitMission(running);
        w2.sim.advanceTime(1);
        System.out.println("\nMission in progress: " + running.getState());
        w2.colony.getEnvironment().setWeatherCondition(WeatherCondition.DUST_STORM);
        System.out.println("A dust storm begins...");
        w2.sim.advanceTime(1);
        outcome(running);
    }

    // Extra: fire in the lab with injured personnel and follow-up medical emergencies
    static void extraFire() {
        title("Extra A: Fire in the Laboratory + Medical Emergency");
        World w = new World();
        w.scientist.setHealth(80);
        w.report("BEFORE");
        Fire fire = new Fire(w.lab, w.hab);
        w.handler.handle(fire);
        System.out.println("Fire: " + fire.getState() + " | Lab operational: " + w.lab.getOperationalStatus());
        System.out.println("Scientist health after fire + treatment: " + w.scientist.getHealth());
        w.report("AFTER");
    }

    // Extra: rover breakdown during an exploration mission
    static void extraRover() {
        title("Extra B: Rover Breakdown during Exploration");
        World w = new World();
        Mission m = new ExplorationMission("MX", "Long exploration", 4,
                agents(w.scientist, w.rover), resources("FUEL", 5));
        w.colony.submitMission(m);
        w.rover.setLocation(MartianSurface.INSTANCE);     // the rover is out on the surface
        w.sim.advanceTime(1);
        System.out.println("Mission: " + m.getState() + ", rover at " + w.rover.getLocation().getLocation());
        RoverBreakdown breakdown = new RoverBreakdown(w.rover);
        w.handler.handle(breakdown);
        outcome(m);
        System.out.println("Breakdown: " + breakdown.getState() + " | rover health: " + w.rover.getHealth()
                + " | rover at " + w.rover.getLocation().getLocation());
        System.out.println("Active mission: " + (w.colony.getActiveMission() == null ? "none" : "ERROR"));
        w.report("AFTER");
    }

    public static void main(String[] args) {
        String which = args.length == 0 ? "all" : args[0];
        boolean all = which.equals("all");
        if (all || which.equals("1")) scenario1();
        if (all || which.equals("2")) scenario2();
        if (all || which.equals("3")) scenario3();
        if (all || which.equals("4")) scenario4();
        if (all || which.equals("5")) scenario5();
        if (all || which.equals("extra")) { extraFire(); extraRover(); }
    }
}
