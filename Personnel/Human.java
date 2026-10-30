package Personnel;
import Core.Location;
import Core.Agent;

abstract public class Human extends Agent{
    private int fatigue; // 0 to 100, 100 being the most fatigued
    
    public Human(String name, Location location, boolean available, int healthy, int fatigue)
    {
        super(name, location, available, healthy);
        
        if(fatigue < 0 || fatigue > 100)
            throw new IllegalArgumentException("Invalid Fatigue Level");
        else 
            this.fatigue = fatigue;
    }
    public void setFatigue(int fatigue)
    {
        if(fatigue < 0 || fatigue > 100)
            throw new IllegalArgumentException("Invalid Fatigue Level");
        else 
            this.fatigue = fatigue;
    }
    public int getFatigue()
    {
        return fatigue;
    }

    @Override
    public boolean isHuman() { return true; }
}
