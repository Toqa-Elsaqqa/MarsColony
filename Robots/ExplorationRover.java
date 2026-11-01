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

    @Override
    public List<Equipment> getEquipment() {
        return Collections.unmodifiableList(equipment);
    }
    @Override
    public boolean isSurfaceRover() { return true; }

    @Override
    public boolean hasOperationalEquipment(Core.EquipmentType... types) {
        for (Equipment item : equipment) {
            if (!item.isOperational()) continue;
            for (Core.EquipmentType type : types)
                if (item.getType() == type) return true;
        }
        return false;
    }
    @Override
    public boolean hasAbilityFor(Skill skill) {
        return skill == Skill.EXPLORATION;
    }
    
}
