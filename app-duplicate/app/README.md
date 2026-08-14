# Goal Tracker — Frontend

React frontend for the Goal Tracker app, bootstrapped with [Create React App](https://github.com/facebook/create-react-app). This is currently a bare scaffold — no real UI has been built yet.

## Getting started

```bash
npm install
npm start
```

Runs the dev server at [http://localhost:3000](http://localhost:3000).

## Backend API

The Spring Boot backend lives at the root of this repo and runs on `http://localhost:8081` (see `../src/main/resources/application.properties`). Start it separately with `../mvnw spring-boot:run` from the repo root.

Each controller is annotated with `@CrossOrigin`, so cross-origin requests are allowed. In development you don't even need to think about the API's host — `package.json` sets `"proxy": "http://localhost:8081"`, so the CRA dev server forwards any request to a relative path (e.g. `fetch('/goals')`, `axios.get('/goals')`) straight to the backend. `axios` is already installed as a dependency if you'd rather use that than `fetch`.

Available endpoints: `/goals`, `/categories`, `/schedules`, `/events`, `/schedule-templates`, `/event-templates` — see the controllers under `../src/main/java/com/harro/goaltracker/controllers` for request/response shapes.

## Other scripts

```bash
npm run build   # production build
npm test        # test runner (no tests currently written)
```
