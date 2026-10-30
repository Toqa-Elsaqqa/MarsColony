package Emergencies;

import Core.Agent;
import Core.Equipment;
import Core.Facility;
import Core.Skill;
import Facilities.Lab;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Fire extends Emergency {
    public static final int EXTRA_OXYGEN_BURN = 20;   // oxygen burned in the lab
    public static final int INJURY = 30;              // health lost by people inside
    public static final int WATER_COST = 10;          // fire-suppression cost

    private final Lab lab;
    private final Facility medicalFacility;           // where the injured get treated
    private final List<Agent> personnelInside = new ArrayList<>();

    public Fire(Lab lab, Facility medicalFacility) {
        super(lab);
        this.lab = lab;
        this.medicalFacility = medicalFacility;
    }

    // One firefighter, no robot required
    @Override public List<Skill> requiredHumanSkills() { return List.of(Skill.FIGHTFIRE); }
    @Override public List<Skill> requiredRobotSkills() { return List.of(); }

    @Override
    public Map<String, Integer> responseCost() {
        return Map.of("WATER", WATER_COST);
    }

    @Override
    protected void applyDetectionEffects(ColonyServices colony) {
        // The lab becomes unavailable (stays so until repaired)
        lab.setOperationalStatus(false);

        // Oxygen consumption increases: burn extra oxygen, never below zero
        int burn = Math.min(EXTRA_OXYGEN_BURN, lab.amountOf("OXYGEN"));
        lab.remove("OXYGEN", burn);

        // People inside are injured (responders are not here yet: detection comes first)
        for (Agent a : colony.agentsAt(lab)) {
            if (a.isHuman()) {
                a.setHealth(Math.max(0, a.getHealth() - INJURY));
                personnelInside.add(a);
            }
        }
    }

    @Override
    protected void applyUnresolvedEffects(ColonyServices colony) {
        // An uncontrolled fire damages all equipment in the lab
        for (Equipment eq : lab.getEquipment())
            eq.damage();
    }

    // Every injured person inside the lab needs medical assistance
    @Override
    public List<Emergency> followUps() {
        List<Emergency> list = new ArrayList<>();
        for (Agent a : personnelInside)
            list.add(new MedicalEmergency(a, medicalFacility));
        return list;
    }
}
