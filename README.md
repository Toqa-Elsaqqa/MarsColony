# MARS-OPS

## Resource Allocation Policy

Mission resources are drawn only from operational facilities. The colony allocates from operational `StorageFacility` instances first, then from the remaining operational facilities in their listed order. A mission's resource request is all-or-nothing: it is checked against colony-wide totals before any resources are consumed. Non-operational facilities do not contribute to resource totals.

## Mission Lifecycle & One-Active-Mission Rule

<<<<<<< Updated upstream
Accepted missions move from `ASSIGNED` to `IN_PROGRESS`, then to `COMPLETED` or `FAILED`. Only one mission may be active at a time. Rejected or failed missions are recorded as failed and are not retried automatically. Assigned agents are released when a mission completes or fails.
=======
| Package | Main types and responsibility |
|---|---|
| `Core` | `Agent`, `Facility`, `Location`, `Resource`, `Equipment`, skills and equipment types |
| `Personnel` | `Human`, `Scientist`, `Engineer`, `MedicalOfficer`, `Firefighter` |
| `Robots` | `Robot`, `ExplorationRover`, `RepairRobot`, `Drone` |
| `Facilities` | `Habitat`, `Lab`, `StorageFacility`, `PowerStation`, `CommunicationStation`, `LifeSupportSystem` |
| `Colony` | `MarsColony` manages agents, facilities, resources and the one active mission |
| `Missions` | `Mission` and the exploration, repair, research and rescue mission types |
| `Environment` | `Environment` and `WeatherCondition` |
| `Simulation` | `Simulation.advanceTime(int)` advances missions, agent condition, and operational habitat oxygen use |
| `Emergencies` | `Emergency`, four emergency types, `EmergencyHandler`, and `ColonyServices` |
| `Reporting` | `StatusReport` renders colony and event history |
>>>>>>> Stashed changes

## Environmental Effects on In-Progress Missions

<<<<<<< Updated upstream
`DUST_STORM` prevents exploration missions from starting and fails active rover exploration missions. `COMMUNICATION_DISRUPTION` blocks communication-dependent remote missions, including rescue coordination, and fails active communication-dependent missions.
=======
- **Inheritance:** `Human` and `Robot` extend `Agent`; concrete crew and robot classes extend those bases. Facilities, missions and emergencies also have abstract base classes and concrete subclasses.
- **Association:** missions use assigned `Agent` instances; emergency types refer to affected facilities or agents.
- **Aggregation:** `MarsColony` holds crew, robots and facilities that are constructed independently and added to it.
- **Composition:** each `Habitat` constructs and owns its `LifeSupportSystem`.
- **Interface:** `MarsColony` implements `ColonyServices`, which decouples `EmergencyHandler` from the concrete colony.

## Resource allocation policy

Colony resource totals include only operational facilities. Consumption checks the full requirement before removing anything, so a shortage consumes zero resources. A valid requirement may be drawn across multiple operational facilities; operational storage facilities are drained first, followed by other facilities in colony order. Invalid or negative requirements are rejected. Habitat oxygen loss is capped by oxygen currently available, so it cannot go below zero.

## Mission lifecycle rules

New missions start in `PENDING`. Accepted missions move through `ASSIGNED` -> `IN_PROGRESS` -> `COMPLETED` or `FAILED`; rejected submissions move from `PENDING` to `FAILED`. Only one mission can be active. Assigned agents become unavailable after acceptance, move to the mission target when simulation starts it, and are released after completion or failure. Mission start and completion are internal transitions; callers advance missions through `Simulation.advanceTime(int)`. Each simulation tick applies polymorphic agent condition changes and consumes one oxygen unit from every operational habitat. A mission failure does not retry automatically.

## Emergency rules

`EmergencyHandler` detects an event, checks preconditions, selects distinct available responders by skill, and verifies response resources before dispatch. If responders or resources are unavailable, the event becomes `UNRESOLVED` and response resources are not consumed. Successful responders are moved to the incident location and released after resolution. An oxygen leak deducts up to 50 oxygen on detection and deducts the same amount again if unresolved; unresolved or depleted habitats become non-operational, while a repaired leak restores operational status. Fire consumes up to 20 oxygen, using local lab oxygen first and colony-wide operational stores for any remainder. Exploration requires operational exploration or scientific equipment; research requires an operational research lab with operational scientific equipment; repair missions require at least one `SPARE_PARTS`.

## Environmental effects on missions

- `DUST_STORM` rejects an exploration mission before activation and fails an active mission using a surface rover (including a rover-based rescue mission) on its next simulation step.
- `COMMUNICATION_DISRUPTION` rejects rescue coordination and remote drone missions before activation. It fails active rescue missions and missions using a `Drone` on the next simulation step.
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
2. **Mission rejection:** an engineer is unsuitable for exploration; mission immediately fails with a reason and never activates.
3. **Resource shortage:** a 600 oxygen request is rejected without consumption; a 450 request is sourced from two operational facilities.
4. **Emergency handling:** resolves an oxygen leak, then demonstrates unresolved leaks and their second oxygen deduction.
5. **Environmental effect:** a dust storm rejects exploration at submission and interrupts an active exploration mission.

## UML diagrams

`docs/` contains editable PlantUML `.puml` and Mermaid `.md` sources for the full merged class diagram, object snapshot, mission state machine, sequence diagrams A (mission execution), B (oxygen leak), C (dust storm), and the use case diagram. The class diagram shows inheritance, association, aggregation and `Habitat` composition of `LifeSupportSystem`. The use case diagram covers nine colony-operator/emergency-operator use cases with `<<include>>` and `<<extend>>` relations.
>>>>>>> Stashed changes
