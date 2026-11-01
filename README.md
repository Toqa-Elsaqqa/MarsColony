# MARS-OPS

Autonomous Mars Colony Management & Emergency Simulator — Computer and Communications Programming II, Lab 5.

MARS-OPS models a Mars research colony with crew, robots, facilities, shared resources, missions, environmental conditions and emergencies. `Main` demonstrates five required scenarios and optional emergency examples.

## Packages

| Package | Main types and responsibility |
|---|---|
| `Core` | `Agent`, `Facility`, `Location`, `Resource`, `Equipment`, skills and equipment types |
| `Personnel` | `Human`, `Scientist`, `Engineer`, `MedicalOfficer`, `Firefighter` |
| `Robots` | `Robot`, `ExplorationRover`, `RepairRobot`, `Drone` |
| `Facilities` | `Habitat`, `Lab`, `StorageFacility`, `PowerStation`, `CommunicationStation`, `LifeSupportSystem` |
| `Colony` | `MarsColony` manages agents, facilities, resources and the one active mission |
| `Missions` | `Mission` and the exploration, repair, research and rescue mission types |
| `Environment` | `Environment` and `WeatherCondition` |
| `Simulation` | `Simulation.advanceTime(int)` advances missions, agent condition and operational habitat oxygen use |
| `Emergencies` | `Emergency`, emergency types, `EmergencyHandler` and `ColonyServices` |
| `Reporting` | `StatusReport` renders colony and event history |

## Object-oriented relationships

- **Inheritance:** `Human` and `Robot` extend `Agent`; concrete crew and robot classes extend those bases. Facilities, missions and emergencies also have abstract base classes and concrete subclasses.
- **Association:** missions use assigned `Agent` instances; emergency types refer to affected facilities or agents.
- **Aggregation:** `MarsColony` holds crew, robots and facilities that are constructed independently and added to it.
- **Composition:** each `Habitat` constructs and owns its `LifeSupportSystem`.
- **Interface:** `MarsColony` implements `ColonyServices`, which decouples `EmergencyHandler` from the concrete colony.

## Resource allocation policy

Mission resources are drawn only from operational facilities. The colony allocates from operational `StorageFacility` instances first, then from the remaining operational facilities in their listed order. A mission's resource request is all-or-nothing: it is checked against colony-wide totals before any resources are consumed. Non-operational facilities do not contribute to resource totals. Valid requirements may be drawn across multiple facilities; invalid or negative requirements are rejected. Habitat oxygen loss is capped by oxygen currently available, so it cannot go below zero.

## Mission lifecycle

New missions start in `PENDING`. Accepted missions move through `ASSIGNED` -> `IN_PROGRESS` -> `COMPLETED` or `FAILED`; rejected submissions move from `PENDING` to `FAILED`. Only one mission can be active at a time. Assigned agents become unavailable after acceptance, move to the mission target when simulation starts it, and are released after completion or failure. Mission start and completion are controlled by the colony and simulation flow; callers advance missions through `Simulation.advanceTime(int)`. Each simulation tick applies polymorphic agent condition changes and consumes one oxygen unit from every operational habitat. Rejected or failed missions are recorded and are not retried automatically.

Missions validate their required skills, equipment, facilities and resources before activation. Exploration requires a surface rover, exploration and research skills, and operational exploration or scientific equipment. Research requires an operational research lab with operational scientific equipment. Repair missions require human and robot repair skills, a repair target and at least one `SPARE_PARTS`.

## Emergency rules

`EmergencyHandler` detects an event, checks preconditions, selects distinct available responders by skill, and verifies response resources before dispatch. If responders or resources are unavailable, the event becomes `UNRESOLVED` and response resources are not consumed. Successful responders are moved to the incident location and released after resolution. All response costs below are deducted from operational colony resources only after suitable responders are found.

| Emergency | Required responders | Response resource cost | Other effects |
|---|---|---|---|
| Oxygen Leak | One human with `REPAIR` and one robot with `REPAIR` | 2 `SPARE_PARTS`, 5 `BATTERY_POWER` | Up to 50 `OXYGEN` is lost on detection and another 50 if unresolved; an unresolved leak makes the habitat non-operational, while successful repair restores it. |
| Fire | One human with `FIGHTFIRE`; no robot responder | 10 `WATER` | Burns up to 20 `OXYGEN` (lab stores first, then colony stores), makes the lab non-operational, and injures each human present by 30 health. An unresolved fire damages the lab's equipment. Each injured person triggers a Medical Emergency follow-up. |
| Rover Breakdown | One human with `REPAIR` and one robot with `REPAIR` | 2 `SPARE_PARTS`, 5 `BATTERY_POWER` | The rover's health drops to 20 and its active mission fails. Successful repair restores rover health to 100; otherwise it remains damaged. |
| Medical Emergency | One human with `HEALING`; no robot responder | 2 `MEDICAL_SUPPLIES`, 5 `OXYGEN` | Successful treatment restores up to 40 health (capped at 100); unresolved treatment costs 10 health. The treatment facility must be operational. |

## Environmental effects on missions

- `DUST_STORM` prevents exploration missions from starting and fails active missions using a surface rover on their next simulation step.
- `COMMUNICATION_DISRUPTION` blocks missions that require remote drone control at submission and fails those active remote-control missions on their next step. Rescue coordination is also rejected during a disruption. Other active mission types continue unless another requirement or failure guard applies.
- Other mission types continue under these conditions unless their own requirements fail.

## Build and run

Requires JDK 8 or newer. From the repository root, in Windows PowerShell (source and target compatibility are Java 8):

```powershell
$sources = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force out | Out-Null
javac -source 1.8 -target 1.8 -d out $sources
java -cp out Main
```

Run one required scenario with `java -cp out Main 1` through `java -cp out Main 5`. Run the additional fire and rover breakdown demonstrations with `java -cp out Main extra`.

## Demonstration scenarios

1. **Successful exploration:** scientist, exploration rover and equipment; prints status before and after and advances to `COMPLETED`.
2. **Mission rejection:** an unsuitable engineer is rejected for exploration; the mission fails with a reason and never activates.
3. **Resource shortage:** a 600 oxygen request is rejected without consumption; a 450 request is sourced from two operational facilities.
4. **Emergency handling:** resolves an oxygen leak, then demonstrates unresolved leaks and their second oxygen deduction.
5. **Environmental effect:** a dust storm rejects exploration at submission and interrupts an active rover mission.

## UML documentation

`docs/` contains editable PlantUML `.puml` and Mermaid `.md` sources and exported PNG diagrams for the merged class diagram, object snapshot, mission state machine, sequence diagrams A (mission execution), B (oxygen leak), C (dust storm), and the use case diagram. The class diagram shows inheritance, association, aggregation and `Habitat` composition of `LifeSupportSystem`. The use case diagram covers colony-operator and emergency-operator use cases with valid `<<include>>` and `<<extend>>` relations.
