package Core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class Facility implements Location{
    private int capacity;
    private boolean operational;
    private static int facCounter = 0;
    public final int facId;
    private String location;
    
    public Facility(int capacity, boolean operational, String location)
    {
        if (capacity <= 0 || capacity > 100)
            throw new IllegalArgumentException("Invalid Capacity for a Facility");
        else 
            this.capacity = capacity;
        
        this.location = location;
        this.operational = operational;
        facCounter++;
        facId = facCounter;
    }
    abstract public String getFacId();
    
   
    private final Map<String, Resource> ledger = new HashMap<>();

    public final int amountOf(String type) {
        Resource resource = ledger.get(type);
        return resource == null ? 0 : resource.getQuantity();
    }

    public final void add(Resource resource) {
        Resource existing = ledger.get(resource.getType());

        if (existing == null) {
            ledger.put(resource.getType(), resource);
        } else {
            existing.add(resource.getQuantity());
        }
    }

    public final boolean remove(String type, int amount) {
        Resource resource = ledger.get(type);

        if (resource == null) {
            return amount == 0;
        }

        return resource.remove(amount);
    }

    private final List<Equipment> equipment = new ArrayList<>();

    public void addEquipment(Equipment item) {
        if (item == null)
            throw new IllegalArgumentException("Equipment cannot be null");
        equipment.add(item);
    }

    public List<Equipment> getEquipment() {
        return Collections.unmodifiableList(equipment);
    }

    public String getLocation() {
        return location;
    }
    public boolean getOperationalStatus()
    {
        return operational;
    }
    public void setOperationalStatus(boolean operational)
    {
        this.operational = operational;
    }
    public int getCapacity()
    {
        return capacity;
    }
}
