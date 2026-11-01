# Mission State Machine

```mermaid
stateDiagram-v2
    [*] --> ASSIGNED: accepted
    [*] --> FAILED: rejected / invalid requirements
    ASSIGNED --> IN_PROGRESS: start
    IN_PROGRESS --> COMPLETED: duration reached
    IN_PROGRESS --> FAILED: hazard, depleted battery, or equipment failure
    COMPLETED --> [*]: release agents
    FAILED --> [*]: record reason and release agents
```
