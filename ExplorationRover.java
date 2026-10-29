package Robots;
import Core.Equipment;
import Core.Location;
import Core.Skill;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExplorationRover extends Robot{
    private final List<Equipment> equipment = new ArrayList<>();
    
    public ExplorationRover(String name, Location location, boolean available, int healthy, int battery) {
        super(name, location, available, healthy, battery);
    }
    public void addEquipment(Equipment item) {
    if (item == null) {
        throw new IllegalArgumentException("Equipment cannot be null");
    }

    equipment.add(item);
    }

    public List<Equipment> getEquipment() {
        return Collections.unmodifiableList(equipment);
    }
    @Override
    public boolean hasAbilityFor(Skill skill) {
        return skill == Skill.EXPLORATION;
    }
    
}