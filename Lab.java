package Facilities;

import Core.Facility;
import Core.Location;

public class Lab extends Facility{
    
    public Lab(int capacity, boolean operational, String location) {
        super(capacity, operational, location);
    }
    
    @Override
    public String getFacId() {
        return "LAB-" + facId;
    }

}