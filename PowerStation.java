package Facilities;

import Core.Facility;

public class PowerStation extends Facility{

    public PowerStation(int capacity, boolean operational, String location) {
        super(capacity, operational, location);
    }

    @Override
    public String getFacId() {
        return "PWRSTATION-" + facId;
    }
    
}
