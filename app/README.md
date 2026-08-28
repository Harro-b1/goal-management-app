# Goal Tracker — Frontend

React frontend for the Goal Tracker app, built with [Vite](https://vite.dev/).

## Getting started

```bash
npm install
npm start
```

Runs the dev server at [http://localhost:5173](http://localhost:5173).

## Backend API

The Spring Boot backend lives at the root of this repo and runs on `http://localhost:8081` (see `../src/main/resources/application.properties`). Start it separately with `../mvnw spring-boot:run` from the repo root.

Each controller is annotated with `@CrossOrigin`, so cross-origin requests are allowed. In development you don't even need to think about the API's host — `vite.config.js` proxies each API path (`/goals`, `/categories`, `/schedules`, `/events`, `/schedule-templates`, `/event-templates`) to `http://localhost:8081`, so a relative request (e.g. `fetch('/goals')`, `axios.get('/goals')`) is forwarded straight to the backend. `axios` is already installed as a dependency if you'd rather use that than `fetch`.

Adding a new endpoint? Add its path to the `server.proxy` block in [`vite.config.js`](./vite.config.js) as well.

Available endpoints: `/goals`, `/categories`, `/schedules`, `/events`, `/schedule-templates`, `/event-templates` — see the controllers under `../src/main/java/com/harro/goaltracker/controllers` for request/response shapes.

## Other scripts

```bash
npm run build     # production build (outputs to build/)
npm run preview   # locally preview the production build
```
