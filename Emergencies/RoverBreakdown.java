package Emergencies;

import Core.Skill;
import Robots.ExplorationRover;
import java.util.List;
import java.util.Map;

public class RoverBreakdown extends Emergency {
    public static final int DAMAGED_HEALTH = 20;      // rover health after the breakdown
    public static final int SPARE_PARTS_COST = 2;     // repair cost (same as an oxygen leak repair)
    public static final int BATTERY_COST = 5;

    private final ExplorationRover rover;

    // The emergency location is wherever the rover is when it breaks down
    // (the Martian surface during an exploration mission), so it stays there.
    public RoverBreakdown(ExplorationRover rover) {
        super(rover.getLocation());
        this.rover = rover;
    }

    // Repair needs one engineer and one repair robot (both with the REPAIR skill)
    @Override public List<Skill> requiredHumanSkills() { return List.of(Skill.REPAIR); }
    @Override public List<Skill> requiredRobotSkills() { return List.of(Skill.REPAIR); }

    @Override
    public Map<String, Integer> responseCost() {
        return Map.of("SPARE_PARTS", SPARE_PARTS_COST,
                      "BATTERY_POWER", BATTERY_COST);
    }

    // Rover status becomes damaged and its mission fails.
    // The rover location is not changed: it remains where it broke down.
    @Override
    protected void applyDetectionEffects(ColonyServices colony) {
        rover.setHealth(DAMAGED_HEALTH);
        colony.failActiveMissionInvolving(rover, "Rover breakdown");
    }

    // Successful repair: the rover is fully repaired (it stays where it is)
    @Override
    protected void applyResolutionEffects(ColonyServices colony) {
        rover.setHealth(100);
    }

    // If unresolved, the rover simply stays damaged and stranded
    @Override
    protected void applyUnresolvedEffects(ColonyServices colony) { }

    public ExplorationRover getRover() { return rover; }
}
