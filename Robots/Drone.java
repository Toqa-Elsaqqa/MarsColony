package Robots;
import Core.Location;
import Core.Skill;

public class Drone extends Robot{

    public Drone(String name, Location location, boolean available, int healthy, int battery) {
        super(name, location, available, healthy, battery);
    }

    @Override
    public boolean hasAbilityFor(Skill skill) {
        return skill == Skill.RECONNAISSANCE;
    }
    
}
