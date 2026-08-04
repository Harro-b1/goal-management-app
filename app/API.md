# Goal Tracker API Reference

Backend base URL in local dev: `http://localhost:8081`. `app/package.json` sets `"proxy": "http://localhost:8081"`, so relative fetches from the CRA dev server (`fetch("/goals")`) are forwarded automatically — you shouldn't need to hardcode the host.

Every controller has `@CrossOrigin` with no restricted origin (there's no auth in this app yet).

## Error responses

Every non-2xx response (except `404`/`204`, which have no body) returns a **plain text string** as the body — not JSON. `Content-Type` is `text/plain`, so don't `response.json()` these; use `response.text()`.

| Status | Cause | Body example |
|---|---|---|
| `400` | A required field/relation was missing (`null`) | `"schedule cannot be null"` |
| `404` | No entity with that path id | *(empty body)* |
| `409` | Write collided with a `UNIQUE` DB constraint | `"Duplicate value for name"` |
| `409` | Any other DB-level conflict not otherwise categorized | *(empty body)* |
| `422` | A relation id was provided but doesn't reference an existing row | `"Invalid goal reference id"` |

The `400`/`409`/`422` messages above name the **DTO field** (camelCase), not the raw DB column — e.g. a missing `startTime` reports `"startTime cannot be null"`, not `"start_time cannot be null"`.

## Data shapes

**`CategoryDto`**
```json
{ "id": 1, "name": "Health" }
```

**`GoalDto`**
```json
{
  "id": 1,
  "name": "Run a 5k",
  "description": "Train and complete a 5k run",
  "completed": false,
  "category": 1,
  "priority": "MEDIUM",
  "finishByDate": "2026-12-31"
}
```
`category` is optional (nullable). `priority` is one of `"LOW" | "MEDIUM" | "HIGH"`. `finishByDate` is optional (nullable), ISO-8601 (`YYYY-MM-DD`).

**`ScheduleDto`**
```json
{ "id": 1, "date": "2026-07-28" }
```
`date` is ISO-8601 (`yyyy-MM-dd`).

**`ScheduleTemplateDto`**
```json
{ "id": 1, "name": "Weekday Routine" }
```

**`EventDto`**
```json
{
  "id": 1,
  "goal": 1,
  "schedule": 1,
  "name": "Morning run",
  "startTime": "07:00:00",
  "endTime": "07:30:00"
}
```
`schedule` is **required**. `goal` is optional (nullable). `startTime`/`endTime` are ISO-8601 (`HH:mm:ss`).

**`EventTemplateDto`**
```json
{
  "id": 1,
  "goal": 1,
  "scheduleTemplate": 1,
  "name": "Morning run",
  "startTime": "07:00:00",
  "endTime": "07:30:00"
}
```
`scheduleTemplate` is **required**. `goal` is optional (nullable).

### `PUT` semantics — read before wiring up an edit form

`PUT` endpoints are full replacements, not patches. Jackson can't distinguish an **omitted** field from an explicit `null`, so:
- Omitting an optional relation (`category`, `goal`) has the same effect as sending `null` — **it clears the relation.**
- Omitting a required relation (`schedule`, `scheduleTemplate`) returns `400`, same as sending `null`.

So every `PUT` request body must include the *current* value of every field the client isn't intentionally changing — including relation ids — or they'll be wiped/rejected.

### `PATCH` semantics — the opposite contract from `PUT`

Every resource that has a `PUT` also has a `PATCH` at the same path, and it's a genuine partial update: any field **omitted or sent as `null`** is left untouched, not cleared. This applies to relation fields too (`category`, `goal`, `schedule`, `scheduleTemplate`) — sending `{"name": "New name"}` to `PATCH /events/1` changes only the name; `schedule`/`goal` stay exactly as they were.

One consequence of "null means untouched": **there is no way to explicitly clear a nullable field via `PATCH`** — not `category`, not `goal`, not a nullable scalar like `Goal.description`. Sending `null` for one of these is indistinguishable from omitting it; both leave the current value in place. If you need to clear a field, use `PUT` with the full current object and that field set to `null`.

`GoalDto.completed` is a special case worth calling out explicitly: **`PATCH /goals/{id}` never changes `completed`, even if you include it in the request body.** Use the dedicated `PUT /goals/{id}/complete` / `PUT /goals/{id}/uncomplete` endpoints for that field instead.

Validation behavior on `PATCH` mirrors `PUT` for whatever *is* provided — an invalid relation id still returns `422`, and (for `Event`/`EventTemplate`) `schedule`/`scheduleTemplate` still can't be set to `null` — but since omitting them just leaves the existing (already-valid) value in place, there's no `400` case to hit on `PATCH` the way there is on `POST`/`PUT`.

---

## Categories — `/categories`

| Method | Path | Body | Success | Notes |
|---|---|---|---|---|
| GET | `/categories` | – | `200` `CategoryDto[]` | |
| GET | `/categories/{id}` | – | `200` `CategoryDto` / `404` | |
| POST | `/categories` | `CategoryDto` | `201` `CategoryDto` + `Location` header | `name` is `UNIQUE` and `NOT NULL` — duplicate → `409`, missing → `400` |
| PUT | `/categories/{id}` | `CategoryDto` | `200` `CategoryDto` / `404` | Same `name` constraints as create |
| PATCH | `/categories/{id}` | `CategoryDto` | `200` `CategoryDto` / `404` | Partial update — omitted `name` leaves it unchanged. Still `409` on a duplicate `name` if provided |
| DELETE | `/categories/{id}` | – | `204` / `404` | **Strips** (doesn't cascade-delete) this category from any `Goal` referencing it — those goals survive with `category: null` |

## Goals — `/goals`

| Method | Path | Body | Success | Notes |
|---|---|---|---|---|
| GET | `/goals` | – | `200` `GoalDto[]` | |
| GET | `/goals/{id}` | – | `200` `GoalDto` / `404` | |
| GET | `/goals/search?query={regex}` | – | `200` `GoalDto[]` | Regex match against `name` or `description` (MySQL `REGEXP`) |
| GET | `/goals/chat?query={text}` | – | `200` `string` | Experimental — proxies to a local Ollama model via langchain4j. Not core to the app; don't build production UI against it |
| POST | `/goals` | `GoalDto` | `201` `GoalDto` + `Location` header | `category` optional — if provided, must reference an existing category (`422` if not); `name`/`priority` required (`400` if missing — `priority` has a DB-level `DEFAULT`, but it only applies when the column is omitted from the `INSERT` entirely, which never happens here, so sending `null` still fails). `finishByDate` optional (nullable) |
| PUT | `/goals/{id}` | `GoalDto` | `200` `GoalDto` / `404` | Same as create. Omitting/nulling `category` or `finishByDate` clears it (see PUT semantics above) |
| PATCH | `/goals/{id}` | `GoalDto` | `200` `GoalDto` / `404` | Partial update — omitted fields (including `category`, `finishByDate`) left unchanged; invalid `category` still `422`. **Never touches `completed`** — use the `complete`/`uncomplete` endpoints below for that |
| PUT | `/goals/{id}/complete` | – | `200` `GoalDto` / `404` | Sets `completed: true`. No body |
| PUT | `/goals/{id}/uncomplete` | – | `200` `GoalDto` / `404` | Sets `completed: false`. No body |
| DELETE | `/goals/{id}` | – | `204` / `404` | **Strips** this goal from any `Event`/`EventTemplate` referencing it — those survive with `goal: null` |

## Schedules — `/schedules`

| Method | Path | Body | Success | Notes |
|---|---|---|---|---|
| GET | `/schedules` | – | `200` `ScheduleDto[]` | |
| GET | `/schedules/{id}` | – | `200` `ScheduleDto` / `404` | |
| GET | `/schedules/{id}/events` | – | `200` `EventDto[]` (empty array if id doesn't exist — no `404`) | All events on that schedule |
| GET | `/schedules/getByDate/{date}` | – | `200` `ScheduleDto` / `404` | `date` path param format is `dd-MM-yyyy`, e.g. `/schedules/getByDate/28-07-2026` — **not** the same format the DTO serializes (`yyyy-MM-dd`) |
| POST | `/schedules` | `ScheduleDto` | `201` `ScheduleDto` + `Location` header | `date` is `UNIQUE` and `NOT NULL` — duplicate → `409`, missing → `400` |
| PUT | `/schedules/{id}` | `ScheduleDto` | `200` `ScheduleDto` / `404` | Same `date` constraints as create |
| PATCH | `/schedules/{id}` | `ScheduleDto` | `200` `ScheduleDto` / `404` | Partial update — omitted `date` leaves it unchanged. Still `409` on a duplicate `date` if provided |
| DELETE | `/schedules/{id}` | – | `204` / `404` | **Cascades delete** to every `Event` on this schedule (unlike Category/Goal — the `Event.schedule` FK is `NOT NULL`, so there's no valid "unlinked" state) |
| POST | `/schedules/template/{id}` | `ScheduleDto` | `201` `ScheduleDto` + `Location` header / `404` if template id doesn't exist | Instantiates a new `Schedule` (from the request body's `date`) plus one `Event` per `EventTemplate` on the given `ScheduleTemplate` |

## Schedule Templates — `/schedule-templates`

| Method | Path | Body | Success | Notes |
|---|---|---|---|---|
| GET | `/schedule-templates` | – | `200` `ScheduleTemplateDto[]` | |
| GET | `/schedule-templates/{id}` | – | `200` `ScheduleTemplateDto` / `404` | |
| GET | `/schedule-templates/{id}/events` | – | `200` `EventTemplateDto[]` (empty array if id doesn't exist — no `404`) | All event templates on that schedule template |
| POST | `/schedule-templates` | `ScheduleTemplateDto` | `201` `ScheduleTemplateDto` + `Location` header | `name` is `UNIQUE` and `NOT NULL` — duplicate → `409`, missing → `400` |
| PUT | `/schedule-templates/{id}` | `ScheduleTemplateDto` | `200` `ScheduleTemplateDto` / `404` | Same `name` constraints as create |
| PATCH | `/schedule-templates/{id}` | `ScheduleTemplateDto` | `200` `ScheduleTemplateDto` / `404` | Partial update — omitted `name` leaves it unchanged. Still `409` on a duplicate `name` if provided |
| DELETE | `/schedule-templates/{id}` | – | `204` / `404` | **Cascades delete** to every `EventTemplate` on this template (the FK is `NOT NULL`) |

## Events — `/events`

| Method | Path | Body | Success | Notes |
|---|---|---|---|---|
| GET | `/events` | – | `200` `EventDto[]` | |
| GET | `/events/{id}` | – | `200` `EventDto` / `404` | |
| POST | `/events` | `EventDto` | `201` `EventDto` + `Location` header | `schedule` required — missing → `400`, references a nonexistent schedule → `422`. `goal` optional — if provided, must reference an existing goal (`422` if not). `name`/`startTime`/`endTime` required (`400` if missing) |
| PUT | `/events/{id}` | `EventDto` | `200` `EventDto` / `404` | Same validation as create. Omitting/nulling `goal` clears it; `schedule` cannot be cleared (it's required) |
| PATCH | `/events/{id}` | `EventDto` | `200` `EventDto` / `404` | Partial update — omitted `schedule`/`goal` left unchanged; invalid ids still `422` |
| DELETE | `/events/{id}` | – | `204` / `404` | No cascading effects — an `Event` has nothing depending on it |

## Event Templates — `/event-templates`

| Method | Path | Body | Success | Notes |
|---|---|---|---|---|
| GET | `/event-templates` | – | `200` `EventTemplateDto[]` | |
| GET | `/event-templates/{id}` | – | `200` `EventTemplateDto` / `404` | |
| POST | `/event-templates` | `EventTemplateDto` | `201` `EventTemplateDto` + `Location` header | `scheduleTemplate` required — missing → `400`, references a nonexistent schedule template → `422`. `goal` optional — if provided, must reference an existing goal (`422` if not). `name`/`startTime`/`endTime` required (`400` if missing) |
| PUT | `/event-templates/{id}` | `EventTemplateDto` | `200` `EventTemplateDto` / `404` | Same validation as create. Omitting/nulling `goal` clears it; `scheduleTemplate` cannot be cleared (it's required) |
| PATCH | `/event-templates/{id}` | `EventTemplateDto` | `200` `EventTemplateDto` / `404` | Partial update — omitted `scheduleTemplate`/`goal` left unchanged; invalid ids still `422` |
| DELETE | `/event-templates/{id}` | – | `204` / `404` | No cascading effects — an `EventTemplate` has nothing depending on it |
