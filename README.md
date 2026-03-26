# fullstack-IDATT2105
Restaurant management application

## Backend Structure

This class diagram gives a high-level view of the backend domain model for the restaurant management system.
It shows how organizations own operational data, how users are connected to logged activity, and how checklists are structured into tasks and task history.

Key relationships:
- One `Organization` has many `User`, `Checklist`, `TemperatureLog`, and `Deviation` entries.
- One `Checklist` contains many `Task` items, and each `Task` can have multiple `TaskLog` records.
- A `User` can create many `TaskLog`, `TemperatureLog`, and `Deviation` records.

```mermaid
classDiagram

class Organization {
  Long id
  String name
}

class User {
  Long id
  String username
  String password
  Role role
}

class Checklist {
  Long id
  String name
  String type
}

class Task {
  Long id
  String title
  boolean completed
}

class TaskLog {
  Long id
  LocalDateTime timestamp
}

class TemperatureLog {
  Long id
  double value
  LocalDateTime timestamp
}

class Deviation {
  Long id
  String description
  String severity
  String status
}

class Role {

  ADMIN
  MANAGER
  EMPLOYEE
}

Organization "1" --> "*" User
Organization "1" --> "*" Checklist
Organization "1" --> "*" TemperatureLog
Organization "1" --> "*" Deviation

Checklist "1" --> "*" Task
Task "1" --> "*" TaskLog

User "1" --> "*" TaskLog
User "1" --> "*" TemperatureLog
User "1" --> "*" Deviation

User "*" --> "1" Organization
Checklist "*" --> "1" Organization
TemperatureLog "*" --> "1" Organization
Deviation "*" --> "1" Organization
```