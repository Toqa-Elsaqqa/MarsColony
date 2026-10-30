package Robots;
import Core.Location;
import Core.Agent;

abstract public class Robot extends Agent {
    private int battery; // between 0 and 100 inclusive
    public Robot(String name, Location location, boolean available, int healthy, int battery)
    {
        super(name, location, available, healthy);
        
        if(battery < 0 || battery > 100)
            throw new IllegalArgumentException("Invalid Battery Level");
        else 
            this.battery = battery;
    }
     public void setBattery(int battery)
    {
        if(battery < 0 || battery > 100)
            throw new IllegalArgumentException("Invalid Battery Level");
        else 
            this.battery = battery;
    }
    public int getBattery()
    {
        return battery;
    }

    @Override
    public boolean isHuman() { return false; }
}
