# MARS-OPS - Autonomous Mars Colony Management & Emergency Simulator
Computer and Communications Programming II - Lab 5 - Fall 2026

## 1. System description and main components
MARS-OPS is a Java simulation of a small Mars research colony. It manages people, robots, facilities and
resources, runs missions over simulated time, reacts to the Martian environment and handles emergencies.

| Package | Responsibility |
|---|---|
| `Core` | `Agent` (shared base of humans and robots), `Facility`, `Location`, `MartianSurface`, `Resource`, `Equipment`, `Skill` |
| `Personnel` | `Human` -> `Scientist`, `Engineer`, `MedicalOfficer`, `Firefighter` |
| `Robots` | `Robot` -> `ExplorationRover`, `RepairRobot`, `Drone` |
| `Facilities` | `Habitat` (owns a `LifeSupportSystem`), `Lab`, `StorageFacility`, `PowerStation`, `CommunicationStation` |
| `Colony` | `MarsColony`: owns crew, robots, facilities, resource totals and the single active mission |
| `Missions` | `Mission` and its subclasses (Exploration, Repair, Research, Rescue), `MissionState` |
| `Simulation` | `Simulation.advanceTime(n)`: advances the clock and progresses the active mission |
| `Environment` | `Environment`, `WeatherCondition` (NORMAL, DUST_STORM, COMMUNICATION_DISRUPTION) |
| `Emergencies` | `Emergency` (abstract) -> `OxygenLeak`, `Fire`, `RoverBreakdown`, `MedicalEmergency`; `EmergencyHandler`; `ColonyServices` |
| `Reporting` | `StatusReport`: builds the colony status report |
| (root) | `Main`: runs the demonstration scenarios |

## 2. Important design decisions and assumptions
* **Capability, not type checks.** Every `Agent` answers `canPerform(Skill)`. It is `final` in `Agent` and combines
  availability, the subclass-specific `hasAbilityFor(skill)` and a health threshold (health >= 60). The emergency code
  also asks `agent.isHuman()` instead of using `instanceof`.
* **Emergencies are decoupled from the colony.** `EmergencyHandler` talks to the colony only through the
  `ColonyServices` interface (`findAvailableAgents`, `agentsAt`, `consume`, `failActiveMissionInvolving`), which
  `MarsColony` implements. The handler can be tested with any implementation.
* **Template-method design for emergencies.** `Emergency` fixes the lifecycle (`detect` -> `resolve` or `fail`) and
  each subclass only declares its responders, cost and effects. A closed emergency can never be reopened, so there is
  no automatic retry.
* **Responses are instantaneous.** A response is not spread over time units: when responders and resources exist, the
  emergency is repaired in the same call and the responders are released immediately (they stay at the affected location).
* **Location** is either a `Facility` or `MartianSurface.INSTANCE` (both implement `Location`).
* Emergencies never interrupt the active mission. A responder busy in the active mission is not available (its
  `availability` is false), so it is simply not selected.
* Emergencies are triggered manually (as allowed by Section 9.2); the handler records every handled emergency in an
  ordered history that is shown in the status report.

### UML relationships used
| Relationship | Where | Why |
|---|---|---|
| Inheritance | `Agent` <- `Human`/`Robot` <- concrete types; `Facility` <- concrete facilities; `Emergency` <- 4 emergencies; `Mission` <- 4 missions | "is-a" with different behaviour (polymorphism) |
| Aggregation | `MarsColony` o-- `Human`, `Robot`, `Facility` | Parts exist independently of the colony and are passed in / added to it |
| Composition | `Habitat` *-- `LifeSupportSystem` | The habitat creates its own life-support system; it cannot exist without it |
| Association | `Mission` -- `Agent`; `EmergencyHandler` -- `Emergency`; `EmergencyHandler` -- `ColonyServices`; `Fire` -- `Lab`; `OxygenLeak` -- `Habitat` | Objects use each other without owning each other |

## 3. Resource allocation rules (Section 5.1)
1. Resources are stored in facilities but managed by the colony. The colony total of a resource is the sum over
   **operational** facilities only (a destroyed or non-operational facility does not count).
2. An operation may draw from several facilities (e.g. 300 in Habitat-A + 200 in Storage = 500, so 450 is valid and 600 is not).
3. **All-or-nothing:** `consume` first checks the whole requirement; if anything is short it returns `false` and nothing
   is removed. The operation fails with a clear reason.
4. A local emergency (oxygen leak) reduces only the affected habitat; the colony total drops accordingly.
5. **Allocation policy:** `StorageFacility` instances are drained first, then the other operational facilities in the
   order they were added to the colony.
6. The status report prints the colony-wide totals of OXYGEN, WATER, FOOD, FUEL, BATTERY_POWER, MEDICAL_SUPPLIES and SPARE_PARTS.

## 4. Emergency rules (Section 9)
A responder is **appropriate** if `canPerform(skill)` is true and **available** if it is not busy in the active mission
and is in usable condition. One distinct responder is chosen per required skill. Dispatching moves the agent to the
emergency location. If responders are missing: state `UNRESOLVED`, reason recorded, nothing consumed, no retry.
If responders exist but the response resources are insufficient: also `UNRESOLVED`, nobody is dispatched.

| Emergency | Location | Required responders | Response cost | Effects on detection | If unresolved |
|---|---|---|---|---|---|
| **OxygenLeak** | a Habitat | 1 human + 1 robot with REPAIR (engineer + repair robot) | 2 spare parts, 5 battery | Habitat loses 50 oxygen once (never below 0) | Same 50 deducted again (total 100 vs 50 if repaired) |
| **Fire** | a Lab | 1 human with FIGHTFIRE (firefighter) | 10 water | Lab becomes non-operational; up to 20 oxygen burned in the lab; every human inside loses 30 health and gets a follow-up `MedicalEmergency` | All equipment in the lab is damaged |
| **RoverBreakdown** | where the rover is (surface during exploration) | 1 human + 1 robot with REPAIR | 2 spare parts, 5 battery | Rover health set to 20, the active mission involving it FAILS ("Rover breakdown"), rover stays where it broke down | Rover stays damaged and stranded |
| **MedicalEmergency** | a treatment facility | 1 human with HEALING (medical officer) | 2 medical supplies, 5 oxygen | Precondition: the facility must be operational | Patient loses 10 health (min 0) |

Successful treatment moves the patient to the facility and increases health by 40 (max 100). The leak is a
one-time deduction; it never drains oxygen continuously. A repaired rover returns to full health.
The lab destroyed by a fire stays non-operational until a repair mission restores it.

## 5. Environmental effects (Section 8)
| Condition | Effect |
|---|---|
| NORMAL | No restrictions |
| DUST_STORM | Exploration missions cannot start (rejected with a reason). |
| COMMUNICATION_DISRUPTION | Rescue missions cannot be initiated (rejected with a reason). |

**Missions already in progress when conditions change:** the environment is re-checked on every time unit
(`Mission.step`). An in-progress exploration mission fails on the next time unit after a dust storm begins
("Dust storm interrupted exploration"), and an in-progress rescue mission fails when a communication disruption begins.
Other mission types are not affected. When a mission fails, its agents become available again.

## 6. Mission states (Section 7 / 6.2)
`ASSIGNED -> IN_PROGRESS -> COMPLETED`, or `-> FAILED` at any point before completion.
* Only one mission can be active. A submission that is rejected (another mission active, missing capability,
  insufficient resources, environment) becomes `FAILED` immediately with a reason and never becomes active.
* Accepted missions become `ASSIGNED` (agents marked busy, resources consumed). The first time unit starts them
  (`IN_PROGRESS`); each time unit adds 1 to the elapsed time; at the duration they become `COMPLETED`.
* Failed missions are never retried; a new mission must be created. Agents are released on completion or failure.

## 7. Other implementation decisions
* Health below 60 makes an agent unable to perform missions or respond (`canPerform`).
* `Firefighter` is an extra personnel type (the spec only lists three examples) so that "fire-response personnel" exist.
* Health, fatigue and battery are integers from 0 to 100 and are validated by the setters.
* The Fire effect "oxygen consumption increases" is modelled as a one-time burn of the lab's own oxygen.

## 8. Compile and run
Requires JDK 11+ (tested with JDK 17). From the project root:

```
# Linux / macOS
mkdir -p out && javac -d out $(find . -name "*.java")
java -cp out Main

# Windows (cmd)
dir /s /b *.java > sources.txt
mkdir out
javac -d out @sources.txt
java -cp out Main
```

## 9. Running the demonstration scenarios
`Main` is the testing class. Each scenario builds a fresh colony.

| Command | Scenario |
|---|---|
| `java -cp out Main` | all scenarios |
| `java -cp out Main 1` | 1 - Successful exploration (state before and after) |
| `java -cp out Main 2` | 2 - Mission rejection (unsuitable agent) |
| `java -cp out Main 3` | 3 - Resource shortage (600 rejected, 450 accepted from two facilities) |
| `java -cp out Main 4` | 4 - Oxygen leak: repaired, unresolved (no responder), unresolved (no resources) |
| `java -cp out Main 5` | 5 - Dust storm (blocks a start, interrupts a mission in progress) |
| `java -cp out Main extra` | Extra - fire + medical follow-ups, rover breakdown during exploration |

## 10. UML diagrams
Located in `docs/` (images and editable source). The class, object and state machine diagrams and sequence diagrams A and C
are provided by the other team members.
