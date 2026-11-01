package Facilities;

import Core.Facility;
import Core.Location;

public class Lab extends Facility{

    @Override
    public boolean supportsResearch() { return true; }
    
    public Lab(int capacity, boolean operational, String location) {
        super(capacity, operational, location);
    }
    
    @Override
    public String getFacId() {
        return "LAB-" + facId;
    }

}
