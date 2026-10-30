package Core;

abstract public class Agent {
    private String name;
    private static int idCounter = 0;
    private final int id;
    private Location location;
    private boolean available;
    private int healthy; 
    
    public Agent(String name, Location location, boolean available, int healthy)
    {
        if(name.trim().isEmpty())
            throw new IllegalArgumentException("Invalid Name");
        else 
            this.name = name;
        
        if(location == null)
            throw new IllegalArgumentException("Invalid Location");
        else 
            this.location = location; // create a validation to ensure that it's a facility or a martian surface
        
        idCounter ++;
        id = idCounter;
        
         if(healthy < 0 || healthy > 100)
            throw new IllegalArgumentException("Invalid Health Level");
        else 
            this.healthy = healthy;
        this.available = available;
    }

    abstract protected boolean hasAbilityFor(Skill skill);

    // Ask the agent whether it is a human crew member (no instanceof checks needed)
    abstract public boolean isHuman();
    
    public final boolean canPerform(Skill skill)
    {
        return available && hasAbilityFor(skill) && healthy >= 60 ;
    }
    
    public void setHealth(int healthy)
    {
        if(healthy < 0 || healthy > 100)
            throw new IllegalArgumentException("Invalid Health Level");
        else 
            this.healthy = healthy;
    }
    public int getHealth()
    {
        return healthy;
    }
    public Location getLocation()
    {
        return location;
    }
    public void setLocation(Location location)
    {
      if(location == null)
            throw new IllegalArgumentException("Invalid Location");
        else 
            this.location = location;
    }
    public boolean getAvailability()
    {
        //validation?
        return available;
    }
    public void setAvailability(boolean available)
    {
        //validation?
        this.available = available;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getName()
    {
        //validation?
        return name;
    }
    public String getID()
    {
        //validation?
        return "CRW-" + id;
    }
}
