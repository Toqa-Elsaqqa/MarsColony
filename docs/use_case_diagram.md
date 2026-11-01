# Use Case Diagram

```mermaid
flowchart LR
    Operator[Colony Operator]
    EmergencyOperator[Emergency Operator]
    subgraph System[MARS-OPS]
      UC1((Manage crew members and robotic units))
      UC2((Manage facilities and equipment))
      UC3((Monitor resources))
      UC4((Generate colony status reports))
      UC5((Submit, assign, execute, and monitor missions))
      UC6((Advance simulation time))
      UC7((Monitor environmental conditions))
      UC8((Trigger and handle emergencies))
      UC9((Inspect mission and emergency outcomes))
    end
    Operator --- UC1
    Operator --- UC2
    Operator --- UC3
    Operator --- UC4
    Operator --- UC5
    Operator --- UC6
    Operator --- UC7
    Operator --- UC9
    EmergencyOperator --- UC8
    EmergencyOperator --- UC9
    UC4 -. "<<include>>" .-> UC3
    UC9 -. "<<extend>>: mission outcome recorded" .-> UC5
    UC9 -. "<<extend>>: emergency outcome recorded" .-> UC8
```

The report includes colony resource totals. Mission execution progresses as simulation time advances, but submitting a mission does not itself include a separate time-advancement use case. Outcome inspection is an optional follow-up to mission or emergency handling; the Java status report exposes the corresponding histories.
