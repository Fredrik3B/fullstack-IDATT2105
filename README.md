# fullstack-IDATT2105
Restaurant management application

## Backend Structure

The backend is organized around a tenant-aware restaurant domain.
`OrganizationModel` is the core boundary for most operational data, while user access, checklist execution,
temperature logging, document storage, and deviation reporting are split into focused modules.

Key relationships:
- One `OrganizationModel` can have many `UserModel`, `ChecklistModel`, `DocumentModel`, `DeviationReportModel`, and `JoinRequestModel` records.
- `UserModel` belongs to an organization and can hold many `RoleModel` entries through the `user_roles` join table.
- `ChecklistModel` belongs to an organization and is composed from reusable `TaskTemplate` entries.
- `TasksModel` represents activated checklist tasks for a specific period and links a checklist to a task template.
- `TemperatureMeasurementModel` records a measured value for an activated task, checklist, organization, and user.
- `ChecklistModuleState` and some supporting models currently store `organizationId` directly instead of a JPA relation.

```mermaid
classDiagram

class AuditableEntity {
  Long id
}

class OrganizationModel {
  UUID id
  String name
  String joinCode
}

class UserModel {
  UUID id
  String email
  String password
  String firstName
  String lastName
}

class RoleModel {
  Integer id
  RoleEnum name
}

class JoinRequestModel {
  UUID id
  JoinOrgStatus status
  LocalDateTime createdAt
  LocalDateTime resolvedAt
}

class ChecklistModel {
  String name
  String description
  ChecklistFrequency frequency
  ComplianceArea complianceArea
  String activePeriodKey
  boolean recurring
  boolean displayedOnWorkbench
  boolean active
}

class TaskTemplate {
  String title
  SectionTypes sectionType
  ComplianceArea complianceArea
  String unit
  String meta
  BigDecimal targetMin
  BigDecimal targetMax
  UUID organisationId
}

class TasksModel {
  boolean completed
  boolean flagged
  boolean active
  String meta
  String periodKey
  LocalDateTime endedAt
}

class TemperatureZoneModel {
  String name
  TemperatureZone zoneType
  ComplianceArea complianceArea
  BigDecimal targetMin
  BigDecimal targetMax
  UUID organizationId
}

class TemperatureMeasurementModel {
  ComplianceArea complianceArea
  String periodKey
  BigDecimal valueC
  LocalDateTime measuredAt
}

class DocumentModel {
  Long id
  String name
  String description
  DocumentCategory category
  DocumentModule module
  String externalUrl
  String originalFileName
  String fileType
  Long fileSize
  String storagePath
  LocalDate expiryDate
  LocalDateTime uploadedAt
}

class DeviationReportModel {
  UUID id
  String deviationName
  DeviationSeverity severity
  LocalDateTime occurredAt
  String noticedBy
  String reportedTo
  String processedBy
  String description
  String immediateAction
  String believedCause
  String correctiveMeasures
  String correctiveMeasuresDone
  LocalDateTime createdAt
}

class ChecklistModuleState {
  Long id
  UUID organizationId
  ComplianceArea complianceArea
  LocalDateTime modifiedAt
}



AuditableEntity <|-- ChecklistModel
AuditableEntity <|-- TaskTemplate
AuditableEntity <|-- TasksModel
AuditableEntity <|-- TemperatureZoneModel
AuditableEntity <|-- TemperatureMeasurementModel

OrganizationModel "1" --> "*" UserModel
OrganizationModel "1" --> "*" ChecklistModel
OrganizationModel "1" --> "*" DocumentModel
OrganizationModel "1" --> "*" DeviationReportModel
OrganizationModel "1" --> "*" JoinRequestModel

UserModel "*" --> "1" OrganizationModel
UserModel "*" --> "*" RoleModel
JoinRequestModel "*" --> "1" UserModel : requester
JoinRequestModel "*" --> "1" UserModel : resolvedBy
JoinRequestModel "*" --> "1" OrganizationModel

ChecklistModel "*" --> "1" OrganizationModel
ChecklistModel "*" --> "*" TaskTemplate
TasksModel "*" --> "1" ChecklistModel
TasksModel "*" --> "1" TaskTemplate

TaskTemplate "*" --> "0..1" TemperatureZoneModel
TemperatureMeasurementModel "*" --> "1" ChecklistModel
TemperatureMeasurementModel "*" --> "1" TasksModel
TemperatureMeasurementModel "*" --> "1" OrganizationModel
TemperatureMeasurementModel "*" --> "1" UserModel : recordedBy

DocumentModel "*" --> "1" OrganizationModel
DocumentModel "*" --> "1" UserModel : uploadedBy

DeviationReportModel "*" --> "1" OrganizationModel
DeviationReportModel "*" --> "1" UserModel : reportedByUser

ChecklistModuleState ..> OrganizationModel : organizationId
TaskTemplate ..> OrganizationModel : organisationId
TemperatureZoneModel ..> OrganizationModel : organizationId
```
