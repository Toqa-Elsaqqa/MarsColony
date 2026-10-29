package Facilities;

public class LifeSupportSystem {
    
    public boolean consumeOxygen(Habitat habitat) {
    return habitat.remove("OXYGEN", 1);
    }

    public boolean leakOxygen(Habitat habitat) {
        return habitat.remove("OXYGEN", 2);
    }

}
