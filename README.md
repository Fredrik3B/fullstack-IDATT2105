# Restaurant Management Application

A full-stack application developed to help restaurants manage daily operations, internal control routines, documentation, and team administration in one system.

Live website: `https://internkontroll.site`

## What The App Does

The application helps restaurants manage daily routines and internal control tasks in one place. Users can register, join or create a restaurant organization, complete checklists, log temperatures, manage documents, and get detailed reports on checklist completion and temperature tracking. The system is designed to support safer operations, clearer responsibilities, and better documentation of internal routines.

The project is available both as a locally runnable development setup and as a deployed website for real usage and demonstration.

## Main Features

- Authentication for registering and logging in users
- Create or join a restaurant
- Role-based access control for managing permissions
- Checklist management for daily/weekly/monthly routines
- Temperature logging and compliance tracking
- Document management for storing restaurant files
- Deviation reporting for registering and following up incidents
- Reporting and dashboard views for operational insight

## Tech Stack

- Frontend: Vue 3, Vue Router, Pinia, Axios
- Backend: Java, Spring Boot, Spring Security, Spring Data JPA
- Database: MySQL, with H2 available for development/testing support
- Testing: Vitest, Cypress, JUnit, Mockito
- Documentation: Swagger / OpenAPI

## Project Structure

The project is divided into a frontend and a backend:

- `frontend/restaurantapp-frontend` contains the Vue application, including views, components, stores, API modules, and tests.
- `backend` contains the Spring Boot application, including controllers, services, repositories, models, DTOs, security, and tests.

Main backend packages:
- `common` for shared domain models such as checklists, tasks, documents, and temperature measurements
- `user` for authentication, organizations, roles, and join requests
- `report` for deviation reporting and report-related logic
- `security` for JWT authentication and access control
- `config` for application configuration

Main frontend folders:
- `src/views` for page-level views
- `src/components` for reusable UI and feature components
- `src/stores` for Pinia state management
- `src/api` for communication with backend endpoints
- `src/composables` for reusable frontend logic
- `src/__tests__` for frontend tests

## Prerequisites

Before running the project locally, make sure you have:

- Java 25
- Maven
- Node.js 20.19 or newer
- npm
- MySQL

## Setup And Installation

1. Clone the repository.
2. Start a MySQL database locally, or use the provided [compose.yaml](/Users/oliver/BIDATA/Fullstack/fullstack-IDATT2105/compose.yaml).
3. Create a root `.env` file for backend configuration.
4. Install frontend dependencies.

Frontend installation:

```bash
cd frontend/restaurantapp-frontend
npm install
```

Backend installation:

```bash
cd backend
./mvnw clean install
```

If the Maven wrapper is not available in your environment, use:

```bash
mvn clean install
```

## Environment Variables

The backend loads configuration from a root `.env` file.

Example:

```env
DB_URL=jdbc:mysql://localhost:3306/mydatabase
DB_USERNAME=myuser
DB_PASSWORD=secret
DB_DIALECT=org.hibernate.dialect.MySQLDialect
JWT_SECRET=replace-with-a-long-random-secret
COOKIE_SECURE=false
```

The frontend can optionally use:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Running The Application

Start the backend:

```bash
cd backend
./mvnw spring-boot:run
```

Or:

```bash
mvn spring-boot:run
```

Start the frontend:

```bash
cd frontend/restaurantapp-frontend
npm run dev
```

Default local URLs:
- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080`

Deployed website:
- `https://internkontroll.site`

## Testing

Frontend unit tests:

```bash
cd frontend/restaurantapp-frontend
npm run test:unit
```

Frontend end-to-end tests:

```bash
cd frontend/restaurantapp-frontend
npm run test:e2e
```

Backend tests:

```bash
cd backend
./mvnw test
```

Or:

```bash
mvn test
```

## API Documentation

The backend includes Swagger / OpenAPI support through Springdoc.

When the backend is running, API documentation should be available at:

- `http://localhost:8080/swagger-ui/index.html`

## Example User Flow

1. Register a user account.
2. Create a restaurant organization or join one using a join code.
3. Log in and access the dashboard.
4. Manage checklists and activated tasks.
5. Record temperature measurements and upload documents.
6. Create deviation reports and review reports or admin data.

## Known Limitations And Future Work

- Some support models still use direct `organizationId` fields instead of full entity relationships.
- The backend structure is still evolving, so parts of the domain model may be refined further.
- The README setup assumes a local MySQL environment and could be improved with a full `.env.example`.
- Deployment instructions are not yet documented.

## Contributors

- Fredrik Borbe
- Oliver Higgins
- Christian Remman
- Marius Kiplesund


## Backend Structure

The backend is organized around a tenant-aware restaurant domain.
`OrganizationModel` is the core boundary for most operational data, while user access, checklist execution,
temperature logging, document storage, and deviation reporting are split into focused modules.

Key relationships:
- One `OrganizationModel` groups users, checklists, documents, deviation reports, and join requests.
- `UserModel` belongs to an organization and can hold multiple roles.
- `ChecklistModel` belongs to an organization and reuses `TaskTemplate` entries.
- `TasksModel` represents an activated checklist task for a given period.
- `TemperatureMeasurementModel` is recorded for a task by a user inside an organization.
- Some support models still store `organizationId` directly instead of a full JPA relation.

```mermaid
classDiagram

class OrganizationModel {
  String name
  String joinCode
}

class UserModel {
  String email
}

class RoleModel {
  RoleEnum name
}

class JoinRequestModel {
  JoinOrgStatus status
}

class ChecklistModel {
  String name
  ChecklistFrequency frequency
  ComplianceArea complianceArea
}

class TaskTemplate {
  String title
  SectionTypes sectionType
}

class TasksModel {
  boolean completed
  boolean flagged
  String periodKey
}

class TemperatureZoneModel {
  String name
  TemperatureZone zoneType
}

class TemperatureMeasurementModel {
  BigDecimal valueC
  LocalDateTime measuredAt
}

class DocumentModel {
  String name
  DocumentCategory category
  DocumentModule module
}

class DeviationReportModel {
  String deviationName
  DeviationSeverity severity
}

class ChecklistModuleState {
  ComplianceArea complianceArea
}

OrganizationModel "1" --> "*" UserModel : members
UserModel "*" --> "*" RoleModel : roles
OrganizationModel "1" --> "*" JoinRequestModel : join requests

OrganizationModel "1" --> "*" ChecklistModel : checklists
ChecklistModel "*" --> "*" TaskTemplate : uses
TaskTemplate "*" --> "0..1" TemperatureZoneModel : zone
ChecklistModel "1" --> "*" TasksModel : activated tasks
TasksModel "*" --> "1" TaskTemplate : template
TasksModel "1" --> "*" TemperatureMeasurementModel : measurements
UserModel "1" --> "*" TemperatureMeasurementModel : recorded by

OrganizationModel "1" --> "*" DocumentModel : documents
UserModel "1" --> "*" DocumentModel : uploaded by

OrganizationModel "1" --> "*" DeviationReportModel : deviation reports
UserModel "1" --> "*" DeviationReportModel : reported by

OrganizationModel "1" ..> ChecklistModuleState : module state
```
