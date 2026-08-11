# Goal Tracker

A Spring Boot REST API + React frontend for tracking goals, scheduling events against them, and reusing schedule/event templates. Includes an experimental, local-LLM-backed feature that suggests activities to fill the free time in a schedule.

## Stack

- **Backend**: Spring Boot 4.1.0 (Spring Framework 7), Java 26, MySQL, Hibernate/JPA, MapStruct, Lombok
- **Frontend**: React (Create React App), in `app/` — currently a bare scaffold, no UI built yet
- **LLM (experimental)**: [langchain4j](https://github.com/langchain4j/langchain4j) backed by a local [Ollama](https://ollama.com/) model
- **Testing**: JUnit 5, MockMvc, Testcontainers (real MySQL)
- **CI**: GitHub Actions runs the test suite on every PR into `main`

## Domain model

- **Goal** — optionally belongs to a `Category`, optionally has a `finishByDate`
- **Category** — one-to-many with `Goal`
- **Schedule** (a date) → **Event** (belongs to a `Schedule`, optionally linked to a `Goal`)
- **ScheduleTemplate** (a name) → **EventTemplate** — reusable blueprints; `POST /schedules/template/{id}` instantiates a concrete `Schedule` + `Event`s from one

All six entities support full CRUD (`GET`, `GET /{id}`, `POST`, `PUT /{id}`, `PATCH /{id}`, `DELETE /{id}`), plus resource-specific extras such as searching goals by regex and generating schedules from templates. See `app/API.md` for the full endpoint reference.

## Getting started

### Prerequisites

- Java 26
- MySQL running locally
- Node.js (for the frontend)
- [Ollama](https://ollama.com/) running locally, with the `llama3.2:1b` model pulled — only needed for the `/schedules/{id}/generateEvents` endpoint; the rest of the app works without it

### Backend

Create a `.env` file in the repo root with your local MySQL credentials:

```
DB_NAME=goaltracker
DB_USERNAME=your_mysql_user
DB_PASSWORD=your_mysql_password
```

Then run:

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8081`.

### Frontend

```bash
cd app
npm install
npm start
```

Runs the dev server on `http://localhost:3000`. `app/package.json` proxies relative-path requests (e.g. `fetch('/goals')`) straight to the backend on port 8081, so no CORS setup is needed in development. See `app/README.md` for more.

### Tests

```bash
./mvnw test
```

Requires Docker running locally (tests spin up a real MySQL container via Testcontainers).

## Project layout

```
src/main/java/com/harro/goaltracker/
  controllers/   REST endpoints — thin, delegate to services
  services/
    crud/        business logic per entity (Goal, Category, Event, EventTemplate, Schedule, ScheduleTemplate)
    llm/         Ollama/langchain4j integration for event generation
  entities/      JPA entities
  dtos/          request/response shapes
  mappers/       MapStruct entity <-> DTO mapping
  repositories/  Spring Data JPA repositories
  exceptions/    typed exceptions + global exception handler
  types/         framework-agnostic value types (e.g. TimeSlot)

app/             React frontend (Create React App)
```

## Documentation

- `app/API.md` — full backend API reference for frontend development
- `app/README.md` — frontend-specific setup notes
