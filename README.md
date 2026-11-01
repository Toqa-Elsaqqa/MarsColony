# MARS-OPS

Autonomous Mars Colony Management & Emergency Simulator — Computer and Communications Programming II, Lab 5.

## System description

MARS-OPS models a Mars research colony with crew, robots, facilities, shared resources, missions, environmental conditions, and emergencies. `Main` demonstrates five required scenarios and optional emergency examples.

| Package | Main types and responsibility |
|---|---|
| `Core` | `Agent`, `Facility`, `Location`, `Resource`, `Equipment`, skills and equipment types |
| `Personnel` | `Human`, `Scientist`, `Engineer`, `MedicalOfficer`, `Firefighter` |
| `Robots` | `Robot`, `ExplorationRover`, `RepairRobot`, `Drone` |
| `Facilities` | `Habitat`, `Lab`, `StorageFacility`, `PowerStation`, `CommunicationStation`, `LifeSupportSystem` |
| `Colony` | `MarsColony` manages agents, facilities, resources and the one active mission |
| `Missions` | `Mission` and the exploration, repair, research and rescue mission types |
| `Environment` | `Environment` and `WeatherCondition` |
| `Simulation` | `Simulation.advanceTime(int)` advances missions and agent condition |
| `Emergencies` | `Emergency`, four emergency types, `EmergencyHandler`, and `ColonyServices` |
| `Reporting` | `StatusReport` renders colony and event history |

## Object-oriented relationships

- **Inheritance:** `Human` and `Robot` extend `Agent`; concrete crew and robot classes extend those bases. Facilities, missions and emergencies also have abstract base classes and concrete subclasses.
- **Association:** missions use assigned `Agent` instances; emergency types refer to affected facilities or agents.
- **Aggregation:** `MarsColony` holds crew, robots and facilities that are constructed independently and added to it.
- **Composition:** each `Habitat` constructs and owns its `LifeSupportSystem`.
- **Interface:** `MarsColony` implements `ColonyServices`, which decouples `EmergencyHandler` from the concrete colony.

## Resource allocation policy

Colony resource totals include only operational facilities. Consumption checks the full requirement before removing anything, so a shortage consumes zero resources. A valid requirement may be drawn across multiple operational facilities; operational storage facilities are drained first, followed by other facilities in colony order. Invalid or negative requirements are rejected. Habitat oxygen loss is capped by oxygen currently available, so it cannot go below zero.

## Mission lifecycle rules

Accepted missions move through `ASSIGNED` -> `IN_PROGRESS` -> `COMPLETED` or `FAILED`. Only one mission can be active. Rejected submissions immediately become `FAILED`, include a reason, and never become active. Assigned agents become unavailable after acceptance and are released after completion or failure. A mission failure does not retry automatically. `Simulation.advanceTime(int)` starts and advances the active mission, drains robot battery, and increases human fatigue.

## Emergency rules

`EmergencyHandler` detects an event, checks preconditions, selects distinct available responders by skill, and verifies response resources before dispatch. If responders or resources are unavailable, the event becomes `UNRESOLVED` and response resources are not consumed. Successful responders are moved to the incident location and released after resolution. An oxygen leak deducts up to 50 oxygen on detection and deducts the same amount again if unresolved. A repaired leak incurs only the initial deduction.

## Environmental effects on missions

- `DUST_STORM` rejects an exploration mission before activation and fails an active exploration mission on its next simulation step.
- `COMMUNICATION_DISRUPTION` rejects rescue coordination and remote drone missions before activation. It fails active rescue missions and missions using a `Drone` on the next simulation step.
- Other mission types continue under these conditions unless their own requirements fail.

## Build and run

Requires JDK 8 or newer. From the repository root, in Windows PowerShell:

```powershell
$sources = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force out | Out-Null
javac -d out $sources
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
