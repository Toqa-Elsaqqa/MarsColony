
package Facilities;

import Core.Facility;

public class StorageFacility extends Facility {

    public StorageFacility(int capacity, boolean operational,
                           String location) {
        super(capacity, operational, location);
    }

    @Override
    public String getFacId() {
        return "STO-" + facId;
    }
}
