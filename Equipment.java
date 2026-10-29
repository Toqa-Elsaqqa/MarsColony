
package Core;

public class Equipment {
    private final String equipmentId;
    private final EquipmentType type;
    private boolean damaged;
    private boolean operational;

    public Equipment(String equipmentId, EquipmentType type) {
        this.equipmentId = equipmentId;
        this.type = type;
        this.damaged = false;
        this.operational = true;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public EquipmentType getType() {
        return type;
    }

    public boolean isDamaged() {
        return damaged;
    }

    public boolean isOperational() {
        return operational;
    }

    public void damage() {
        damaged = true;
        operational = false;
    }

    public void repair() {
        damaged = false;
        operational = true;
    }
}