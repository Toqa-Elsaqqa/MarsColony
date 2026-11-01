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
        boolean consumed = sys.consumeOxygen(this);
        if (amountOf("OXYGEN") == 0) setOperationalStatus(false);
        return consumed;
    }
    
    @Override
    public void advanceOneTimeUnit() {
        if (!getOperationalStatus()) return;
        consumeOxygenForOneTimeUnit();
    }

<<<<<<< Updated upstream
=======
    /** Deducts up to {@code amount} oxygen and returns the amount actually lost. */
    public int applyLeak(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Leak amount cannot be negative");
        int lost = Math.min(amount, amountOf("OXYGEN"));
        if (lost > 0) remove("OXYGEN", lost);
        if (amountOf("OXYGEN") == 0) setOperationalStatus(false);
        return lost;
    }

>>>>>>> Stashed changes
    @Override
    public String getFacId()
    {
        return "HAB-" + facId;
    }
}