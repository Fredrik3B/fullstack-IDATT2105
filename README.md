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

- `frontend` contains the Vue application, including views, components, stores, API modules, and tests.
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
cd frontend
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
cd frontend
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
cd frontend
npm run test:unit
```

Frontend end-to-end tests:

```bash
cd frontend
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


## Contributors

- Fredrik Borbe
- Oliver Higgins
- Christian Remman
- Marius Kiplesund

