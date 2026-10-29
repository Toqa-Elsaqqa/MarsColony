package Facilities;

import Core.Facility;

public class CommunicationStation extends Facility{

    public CommunicationStation(int capacity, boolean operational, String location) {
        super(capacity, operational, location);
    }

    @Override
    public String getFacId() {
        return "COMMSTATION-" + facId;
    }
    
}