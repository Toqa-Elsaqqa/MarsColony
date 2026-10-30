package Emergencies;

import Core.Agent;
import Core.Facility;
import Core.Skill;
import java.util.List;
import java.util.Map;

public class MedicalEmergency extends Emergency {
    public static final int MEDICAL_SUPPLIES_COST = 2;
    public static final int OXYGEN_COST = 5;
    public static final int HEALTH_GAIN = 40;
    public static final int HEALTH_LOSS_IF_UNTREATED = 10;

    private final Agent patient;
    private final Facility treatmentFacility;

    public MedicalEmergency(Agent patient, Facility treatmentFacility) {
        super(treatmentFacility);            // also rejects null
        if (patient == null || !patient.isHuman())
            throw new IllegalArgumentException("Patient must be a human crew member");
        this.patient = patient;
        this.treatmentFacility = treatmentFacility;
    }

    // One medical officer (HEALING), no robot needed
    @Override public List<Skill> requiredHumanSkills() { return List.of(Skill.HEALING); }
    @Override public List<Skill> requiredRobotSkills() { return List.of(); }

    @Override
    public Map<String, Integer> responseCost() {
        return Map.of("MEDICAL_SUPPLIES", MEDICAL_SUPPLIES_COST,
                      "OXYGEN", OXYGEN_COST);
    }

    // The relevant facility must be operational
    @Override
    public String preconditionFailure() {
        if (!treatmentFacility.getOperationalStatus())
            return "Treatment facility " + treatmentFacility.getFacId()
                    + " is not operational";
        return null;
    }

    @Override
    protected void applyDetectionEffects(ColonyServices colony) { }

    // Successful treatment: patient moves to the facility and recovers
    @Override
    protected void applyResolutionEffects(ColonyServices colony) {
        patient.setLocation(treatmentFacility);
        patient.setHealth(Math.min(100, patient.getHealth() + HEALTH_GAIN));
    }

    // Untreated patient gets worse (never below zero)
    @Override
    protected void applyUnresolvedEffects(ColonyServices colony) {
        patient.setHealth(Math.max(0, patient.getHealth() - HEALTH_LOSS_IF_UNTREATED));
    }

    public Agent getPatient() { return patient; }
}
