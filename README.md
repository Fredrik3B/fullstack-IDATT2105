# fullstack-IDATT2105
Restaurant management application

## Backend Structure

The backend foundation is centered on a simple tenant-aware domain model.
`Organization` isolates each restaurant's data, while operational entities such as checklists, logs,
deviations, and alcohol compliance records are linked back to both the organization and the user when relevant.

Key relationships:
- One `Organization` owns many `User`, `Checklist`, `TemperatureLog`, `Deviation`, and `AlcoholCompliance` records.
- One `Checklist` contains many `Task` entries.
- One `Task` can have many `TaskLog` records.
- A `User` can create operational records such as task logs, temperature logs, deviations, and alcohol compliance entries.

```mermaid
classDiagram

class AuditableEntity {
  Long id
  LocalDateTime createdAt
  LocalDateTime updatedAt
}

class Organization {
  String name
  String organizationNumber
}

class Checklist {
  String name
  String description
  ChecklistFrequency frequency
  ComplianceArea complianceArea
  boolean active
}

class Task {
  String title
  String description
  int orderIndex
  boolean requiredTask
  boolean active
}

class TaskLog {
  boolean completed
  String notes
  LocalDateTime timestamp
}

class TemperatureLog {
  String location
  BigDecimal value
  TemperatureZone temperatureZone
  BigDecimal minimumAllowed
  BigDecimal maximumAllowed
  LocalDateTime timestamp
}

class Deviation {
  String title
  String description
  DeviationSeverity severity
  DeviationStatus status
  ComplianceArea complianceArea
  LocalDateTime reportedAt
  LocalDateTime resolvedAt
}

class AlcoholCompliance {
  AlcoholComplianceType complianceType
  boolean compliant
  String details
  String notes
  LocalDateTime performedAt
}

class User {
  String username
  String fullName
  String email
  String passwordHash
  Role role
  boolean active
}



AuditableEntity <|-- Organization
AuditableEntity <|-- User
AuditableEntity <|-- Checklist
AuditableEntity <|-- Task
AuditableEntity <|-- TaskLog
AuditableEntity <|-- TemperatureLog
AuditableEntity <|-- Deviation
AuditableEntity <|-- AlcoholCompliance

Organization "1" --> "*" User
Organization "1" --> "*" Checklist
Organization "1" --> "*" TemperatureLog
Organization "1" --> "*" Deviation
Organization "1" --> "*" AlcoholCompliance

Checklist "1" --> "*" Task
Task "1" --> "*" TaskLog

User "1" --> "*" TaskLog
User "1" --> "*" TemperatureLog
User "1" --> "*" Deviation
User "1" --> "*" AlcoholCompliance

User "*" --> "1" Organization
Checklist "*" --> "1" Organization
TemperatureLog "*" --> "1" Organization
Deviation "*" --> "1" Organization
AlcoholCompliance "*" --> "1" Organization
Task "*" --> "1" Checklist
TaskLog "*" --> "1" Task
TaskLog "*" --> "1" User
```
