package Personnel;
import Core.Location;
import Core.Skill;

public class Firefighter extends Human{
    public Firefighter(String name, Location location, boolean available, int healthy, int fatigue) {
        super(name, location, available, healthy, fatigue);
    }

    @Override
    protected boolean hasAbilityFor(Skill skill) {
        return skill == Skill.FIGHTFIRE;
    }   
}