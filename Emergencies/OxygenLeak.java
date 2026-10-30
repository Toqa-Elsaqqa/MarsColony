package Emergencies;

import Core.Skill;
import Facilities.Habitat;
import java.util.List;
import java.util.Map;

public class OxygenLeak extends Emergency {

    // Oxygen lost when the leak is detected (one-time deduction, not a continuous drain).
    // If the leak ends UNRESOLVED, the same amount is deducted again,
    // so an unresolved leak costs 2 * 50 = 100, a repaired one costs 50.
    public static final int OXYGEN_LOSS = 50;

    // Repair cost, taken from the colony ONLY if responders are available
    // (nothing is consumed when the emergency ends UNRESOLVED).
    // Matches the example in the spec: 2 spare parts and 5 battery units.
    public static final int SPARE_PARTS_COST = 2;
    public static final int BATTERY_COST = 5;

    private final Habitat habitat;
    private int totalOxygenLost = 0;

    public OxygenLeak(Habitat habitat) {
        super(habitat);              // the habitat is the emergency's location
        this.habitat = habitat;
    }

    // Oxygen leak needs one human and one robot, both able to repair
    @Override
    public List<Skill> requiredHumanSkills() { return List.of(Skill.REPAIR); }

    @Override
    public List<Skill> requiredRobotSkills() { return List.of(Skill.REPAIR); }

    @Override
    public Map<String, Integer> responseCost() {
        return Map.of("SPARE_PARTS", SPARE_PARTS_COST,
                      "BATTERY_POWER", BATTERY_COST);
    }

    // Called by Emergency.detect(): deduct the initial loss (once per emergency)
    @Override
    protected void applyDetectionEffects(ColonyServices colony) {
        totalOxygenLost += habitat.applyLeak(OXYGEN_LOSS);
    }

    // Called by Emergency.fail(): deduct the same amount again
    @Override
    protected void applyUnresolvedEffects(ColonyServices colony) {
        totalOxygenLost += habitat.applyLeak(OXYGEN_LOSS);
    }

    public Habitat getHabitat() { return habitat; }
    public int getTotalOxygenLost() { return totalOxygenLost; }
}
