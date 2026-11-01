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

`EmergencyHandler` detects an event, checks preconditions, selects distinct available responders by skill, and verifies response resources before dispatch. If responders or resources are unavailable, the event becomes `UNRESOLVED` and response resources are not consumed. Successful responders are moved to the incident location and released after resolution. An oxygen leak deducts up to 50 oxygen on detection and deducts the same amount again if unresolved; unresolved or depleted habitats become non-operational, while a repaired leak restores operational status. Fire consumes up to 20 oxygen, using local lab oxygen first and colony-wide operational stores for any remainder.

## Environmental effects on missions

- `DUST_STORM` prevents exploration missions from starting and fails active missions using a surface rover on their next simulation step.
- `COMMUNICATION_DISRUPTION` blocks remote drone missions and rescue coordination, and fails active communication-dependent missions.
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

`docs/` contains editable PlantUML `.puml` and Mermaid `.md` sources for the merged class diagram, object snapshot, mission state machine, sequence diagrams A (mission execution), B (oxygen leak), C (dust storm), and the use case diagram. The class diagram shows inheritance, association, aggregation and `Habitat` composition of `LifeSupportSystem`. The use case diagram covers colony-operator and emergency-operator use cases with `<<include>>` and `<<extend>>` relations.
