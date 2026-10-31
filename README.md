# MARS-OPS

## Resource Allocation Policy

Mission resources are drawn only from operational facilities. The colony allocates from operational `StorageFacility` instances first, then from the remaining operational facilities in their listed order. A mission's resource request is all-or-nothing: it is checked against colony-wide totals before any resources are consumed. Non-operational facilities do not contribute to resource totals.

## Mission Lifecycle & One-Active-Mission Rule

Accepted missions move from `ASSIGNED` to `IN_PROGRESS`, then to `COMPLETED` or `FAILED`. Only one mission may be active at a time. Rejected or failed missions are recorded as failed and are not retried automatically. Assigned agents are released when a mission completes or fails.

## Environmental Effects on In-Progress Missions

`DUST_STORM` prevents exploration missions from starting and fails active rover exploration missions. `COMMUNICATION_DISRUPTION` blocks communication-dependent remote missions, including rescue coordination, and fails active communication-dependent missions.
