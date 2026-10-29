package Facilities;
import Core.Facility;

public class Habitat extends Facility{
    private final LifeSupportSystem sys;
    public Habitat(int capacity, boolean operational, String location)
    {
        super(capacity, operational, location);
        this.sys = new LifeSupportSystem();
    }
    
    public boolean consumeOxygenForOneTimeUnit() {
        return sys.consumeOxygen(this);
    }

    public boolean applyLeakForOneTimeUnit() {
        return sys.leakOxygen(this);
    }

    @Override
    public String getFacId()
    {
        return "HAB-" + facId;
    }
}