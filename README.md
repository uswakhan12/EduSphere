# EduSphere

**EduSphere** is a desktop STEM learning platform built with Java 21 and JavaFX. It lets creators publish educational content (videos, articles, research papers, presentations), viewers discover and engage with that content, and administrators moderate the community — all in a single offline-capable application with file-based persistence.

---

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Tech Stack](#tech-stack)
4. [Architecture](#architecture)
5. [Domain Model](#domain-model)
6. [Core Services](#core-services)
7. [User Roles & Dashboards](#user-roles--dashboards)
8. [Persistence & Concurrency](#persistence--concurrency)
9. [Project Structure](#project-structure)
10. [Getting Started](#getting-started)
11. [Default Credentials](#default-credentials)
12. [Integration Demo](#integration-demo)
13. [Design Patterns & Principles](#design-patterns--principles)
14. [Data Flow](#data-flow)
15. [License / Notes](#license--notes)

---

## Overview

EduSphere (Maven artifact: `stemplatform:STEM`) is a role-based educational content platform focused on STEM subjects. The application is split into:

- A **domain & service layer** (plain Java) that models users, content, live events, search, downloads, streaming, and notifications.
- A **JavaFX GUI layer** that presents role-specific dashboards on top of those services.
- A **persistence layer** that serializes the full application state to disk (`data/application.dat`).

A headless `IntegrationDemo` exercises the service layer without the UI, which makes the domain logic independently verifiable.

**Tagline (from the login screen):** *Learn. Create. Share.* — a unified STEM content platform for Admins, Creators, and Viewers.

---

## Features

### Authentication & accounts
- Register as a **Viewer** or **Creator**
- Email uniqueness and password rules (minimum 6 characters, basic email format validation)
- Login / logout with a single active session
- Banned users cannot log in
- Default system administrator is seeded on first launch if none exists

### Content publishing (Creators)
- Publish four content types:
  - **Video** — file path + duration; watchable via a streaming session
  - **Article** — body text + estimated reading time
  - **Research Paper** — downloadable file, authors, abstract, publication metadata
  - **Presentation** — downloadable slides, slide count, presentation type
- Subject tagging, titles, and descriptions
- Manage published catalog and host live events

### Viewer engagement
- Browse and search the catalog (title, description, subject)
- Filter by subject or content type
- Like / unlike, comment
- Favorites, Watch Later, and Watch History (personal library)
- Subscribe to creators and receive upload notifications
- Watch videos with play / pause / resume / stop / seek
- Download research papers and presentations (async, non-blocking UI)
- Join / leave capacity-limited live events

### Administration
- Platform overview (user / content / event counts)
- Ban / unban users
- Remove content and comments
- Inspect live events

### Platform infrastructure
- Java serialization persistence to `data/application.dat`
- Periodic background auto-save (every 30 seconds)
- Final save on clean application shutdown
- Asynchronous downloads on a daemon thread pool
- Thread-safe live-event join/leave and download history

---

## Tech Stack

| Layer | Technology |
|--------|------------|
| Language | Java 21 |
| Module system | JPMS (`module-info.java` → `stemplatform.stem`) |
| Build | Apache Maven (wrapper included: `mvnw` / `mvnw.cmd`) |
| UI | JavaFX 21 (`controls`, `fxml`, `web`, `swing`, `media`) |
| UI extras | ControlsFX, FormsFX, ValidatorFX, Ikonli, BootstrapFX, TilesFX, FXGL |
| Persistence | Java Object Serialization (`ObjectOutputStream` / `ObjectInputStream`) |
| Testing harness | `IntegrationDemo` (console assertions; JUnit 5 on classpath for future tests) |

**Main application class:** `stemplatform.stem.gui.EduSphereApp`

---

## Architecture

EduSphere follows a layered, service-oriented desktop architecture. The GUI never owns business rules; it delegates to managers and domain objects wired through a shared `AppContext`.

```
┌─────────────────────────────────────────────────────────────────┐
│                        Presentation (JavaFX)                     │
│  EduSphereApp · LoginView · RegisterView                         │
│  ViewerDashboardView · CreatorDashboardView · AdminDashboardView │
│  UiKit · edusphere.css                                           │
├─────────────────────────────────────────────────────────────────┤
│                     Application Context                          │
│  AppContext — wires services, holds ApplicationState,            │
│               auto-save scheduler, scene root switching          │
├─────────────────────────────────────────────────────────────────┤
│                         Service Layer                            │
│  AuthenticationService · ContentManager · DownloadManager        │
│  NotificationService · SearchEngine · FileManager                │
├─────────────────────────────────────────────────────────────────┤
│                          Domain Layer                            │
│  Users: User → Viewer | Creator | Administrator                  │
│  Content: Content → Video | Article | ResearchPaper | Presentation│
│  Library · LiveEvent · StreamingSession · Notification · Comment │
├─────────────────────────────────────────────────────────────────┤
│                       Persistence Layer                          │
│  ApplicationState  →  FileManager  →  data/application.dat       │
└─────────────────────────────────────────────────────────────────┘
```

### Runtime composition

On startup, `EduSphereApp`:

1. Creates an `AppContext` for the primary `Stage`.
2. Loads (or creates) `ApplicationState` via `FileManager`.
3. Seeds a default administrator if none exists.
4. Shows `LoginView` with the shared stylesheet.
5. On exit (`stop()`), shuts down auto-save and persists state.

`AppContext` is the composition root: it constructs `AuthenticationService`, `ContentManager`, `NotificationService`, and `DownloadManager` against the loaded state lists, and exposes them to every view.

### Package map

| Package | Responsibility |
|---------|----------------|
| `stemplatform.stem.gui` | JavaFX views, app entry, shared UI helpers, context |
| `stemplatform.stem.users` | User hierarchy and role-specific behavior |
| `stemplatform.stem.content` | Content hierarchy, comments, `Downloadable` |
| `stemplatform.stem.authentication` | Registration, login, session current-user |
| `stemplatform.stem.management` | Content publishing/removal, file downloads |
| `stemplatform.stem.search` | Keyword search and subject/type filters |
| `stemplatform.stem.notifications` | Subscriber notifications on new uploads |
| `stemplatform.stem.streaming` | Video playback session (`Streamable`) |
| `stemplatform.stem.events` | Capacity-limited live events |
| `stemplatform.stem.library` | Favorites, watch later, history |
| `stemplatform.stem.storage` | Serializable state blob and file I/O |

---

## Domain Model

### Users

```
User (abstract)
├── Viewer        — library, subscriptions, notifications, events, engagement
├── Creator       — bio, published content, subscribers, live events
└── Administrator — ban users, remove content/comments, inspect content
```

**Shared user fields:** `userId`, `name`, `email`, `password`, `banned`.

| Role | Key capabilities |
|------|------------------|
| **Viewer** | Subscribe, like/comment, library lists, watch videos, attend events, receive notifications |
| **Creator** | Publish content, host live events, manage subscriber set |
| **Administrator** | Ban/unban, remove content via `ContentManager`, remove comments |

### Content

```
Content (abstract)
├── Video              — filePath, duration (seconds)
├── Article            — body, readingTime
├── ResearchPaper      — Downloadable; authors, abstract, publication, date
└── Presentation       — Downloadable; slideCount, presentationType
```

**Shared content fields:** `contentId`, `creator`, `title`, `description`, `subject`, `uploadDate`, `viewCount`, comments, likes.

Interfaces:

- **`Downloadable`** — exposes `getFilePath()` for papers and presentations.
- **`Streamable`** — play/pause/resume/stop/seek contract implemented by `StreamingSession`.

### Supporting entities

| Entity | Role |
|--------|------|
| `Comment` | Text comment authored by a viewer on content |
| `Library` | Per-viewer favorites, watch-later, history |
| `LiveEvent` | Hosted event with max capacity; `currentViewers` is transient runtime state |
| `StreamingSession` | Per-watch session; records one view and adds to history on first `start()` |
| `Notification` | Message + timestamp + read flag for a viewer |
| `ApplicationState` | Aggregate root for persistence: users, content, events |

### Class relationships (simplified)

```
Creator ──publishes──► Content ◄──likes/comments── Viewer
   │                      │                           │
   │                   Video                          │
   │                      │                           │
   │               StreamingSession ◄──watch()────────┘
   │
   └──hosts──► LiveEvent ◄──join/leave── Viewer

Creator ◄──subscribe── Viewer
   │
   └── NotificationService.notifySubscribers(Content)
            └──► Viewer.receiveNotification(...)
```

---

## Core Services

### `AuthenticationService`
- Registers viewers, creators, and administrators (UUID user IDs).
- Enforces unique email, email format, and password length.
- Tracks a single `currentUser`; rejects login if already logged in or banned.
- Lookup by email or user ID.

### `ContentManager`
- Publishes content only if it belongs to the given creator and is not already published.
- Removes content from the global list and from the creator’s published list.
- Lookup by content ID; exposes an immutable copy of all content.

### `DownloadManager`
- **Synchronous** `download()` for tests / simple callers (copies file with `REPLACE_EXISTING`).
- **Asynchronous** `downloadAsync()` on a 2-thread daemon pool so the JavaFX UI stays responsive.
- Thread-safe `downloadHistory` list.
- Callers updating UI from callbacks should use `Platform.runLater(...)`.

### `NotificationService`
- When content is published (from the UI publish flow), notifies all of the creator’s subscribers with a new `Notification`.

### `SearchEngine`
- Case-insensitive substring search across title, description, and subject.
- `filterBySubject` and `filterByType` (matches simple class name, e.g. `"Video"`).

### `FileManager`
- Ensures `data/` exists.
- Saves / loads `ApplicationState` to/from `data/application.dat`.
- On missing or corrupt file, returns a fresh empty state.

---

## User Roles & Dashboards

### Login & registration
- **`LoginView`** — brand panel + credentials; routes by runtime type to the correct dashboard.
- **`RegisterView`** — choose Viewer or Creator (creators supply a bio).

### Viewer dashboard
Navigation: **Browse · Favorites · Watch Later · History · Notifications · Events · Subscriptions**

Typical actions: search/filter catalog, open content detail (like, comment, favorite, watch later, download if applicable), watch video with session controls, join live events, manage subscriptions.

### Creator dashboard
Navigation: **Overview · My Content · Publish · Events · Subscribers**

Typical actions: publish any of the four content types, remove own content, schedule live events, view subscriber list and overview stats.

### Admin dashboard
Navigation: **Overview · Users · Content · Events**

Typical actions: ban/unban users, remove content, review events and platform counts. Admins can also register additional admin accounts from the users area (as implemented in the admin UI).

### Shared UI
- **`UiKit`** — styled fields, buttons, nav helpers, spacers.
- **`edusphere.css`** — application-wide look and feel.

---

## Persistence & Concurrency

### Persistence model
All durable data lives in one serializable `ApplicationState` graph:

```
ApplicationState
├── List<User>       (polymorphic: Viewer / Creator / Administrator)
├── List<Content>    (polymorphic content hierarchy)
└── List<LiveEvent>
```

Stored at:

```text
data/application.dat
```

### Auto-save
`AppContext` schedules a daemon thread that calls synchronized `save()` every **30 seconds**. On shutdown, `EduSphereApp.stop()`:

1. Shuts down the auto-save executor (`shutdownNow()`).
2. Performs a final `save()`.

This reduces data loss if the process is force-closed, while avoiding interleaved writes that could corrupt the file.

### Concurrency highlights
| Area | Approach |
|------|----------|
| Auto-save vs manual save | `synchronized` on `AppContext.save()` |
| Live event capacity | `synchronized` `join()` / `leave()` / count getters |
| Download history | `synchronized` list access across worker threads |
| Downloads | Fixed daemon thread pool (size 2) |
| Transient viewers on events | `currentViewers` is `transient`; restored empty via custom `readObject()` after deserialization (avoids NPE after reload) |

---

## Project Structure

```text
EduSphere/
├── pom.xml                          # Maven project (JavaFX 21, Java 21)
├── mvnw / mvnw.cmd                  # Maven Wrapper
├── .mvn/wrapper/                    # Wrapper properties
├── data/
│   └── application.dat              # Runtime persistent state (created on first save)
├── src/main/
│   ├── java/stemplatform/
│   │   ├── module-info.java
│   │   └── stem/
│   │       ├── IntegrationDemo.java           # Headless service integration harness
│   │       ├── authentication/
│   │       │   └── AuthenticationService.java
│   │       ├── content/
│   │       │   ├── Content.java
│   │       │   ├── Video.java
│   │       │   ├── Article.java
│   │       │   ├── ResearchPaper.java
│   │       │   ├── Presentation.java
│   │       │   ├── Comment.java
│   │       │   └── Downloadable.java
│   │       ├── events/
│   │       │   └── LiveEvent.java
│   │       ├── gui/
│   │       │   ├── EduSphereApp.java          # JavaFX Application entry
│   │       │   ├── AppContext.java
│   │       │   ├── LoginView.java
│   │       │   ├── RegisterView.java
│   │       │   ├── ViewerDashboardView.java
│   │       │   ├── CreatorDashboardView.java
│   │       │   ├── AdminDashboardView.java
│   │       │   └── UiKit.java
│   │       ├── library/
│   │       │   └── Library.java
│   │       ├── management/
│   │       │   ├── ContentManager.java
│   │       │   └── DownloadManager.java
│   │       ├── notifications/
│   │       │   ├── Notification.java
│   │       │   └── NotificationService.java
│   │       ├── search/
│   │       │   └── SearchEngine.java
│   │       ├── storage/
│   │       │   ├── ApplicationState.java
│   │       │   └── FileManager.java
│   │       ├── streaming/
│   │       │   ├── Streamable.java
│   │       │   └── StreamingSession.java
│   │       └── users/
│   │           ├── User.java
│   │           ├── Viewer.java
│   │           ├── Creator.java
│   │           └── Administrator.java
│   └── resources/stemplatform/stem/
│       ├── css/edusphere.css
│       └── hello-view.fxml            # Legacy/sample FXML (UI is code-built)
└── README.md
```

---

## Getting Started

### Prerequisites
- **JDK 21** or newer (the project compiles with `--release 21`)
- No separate JavaFX SDK install is required when using Maven (OpenJFX artifacts are pulled as dependencies)

### Build

```bash
./mvnw clean compile
```

On Windows:

```bash
mvnw.cmd clean compile
```

### Run the GUI

```bash
./mvnw javafx:run
```

This launches `stemplatform.stem.gui.EduSphereApp` via the `javafx-maven-plugin` configured in `pom.xml`.

### IDE
- Open the project as a Maven project (IntelliJ IDEA / NetBeans / Eclipse / VS Code).
- Run `EduSphereApp.main`.
- NetBeans users can also use the `nbactions.xml` `run` action (`clean` + `javafx:run`).

### First-run notes
- The `data/` directory is created automatically if missing.
- If no administrator exists in loaded state, the app registers:

  - **Email:** `admin@edusphere.com`
  - **Password:** `admin123`

---

## Default Credentials

| Role | Email | Password | Notes |
|------|--------|----------|--------|
| System Admin | `admin@edusphere.com` | `admin123` | Seeded automatically when no admin is present |
| Viewer / Creator | *(you register)* | min 6 chars | Via the registration screen |

> Change the default admin password in a real deployment. Passwords are stored in plain text in the serialized state for this academic/demo codebase — not suitable for production security requirements.

---

## Integration Demo

`IntegrationDemo` is a **non-JavaFX** console harness that walks through the full service stack and prints `[PASS]` / `[FAIL]` for each assertion. It covers:

1. Registration & authentication (including rejection cases)
2. Publishing all four content types
3. Subscriptions & notifications
4. Video streaming session lifecycle
5. Likes, comments, library lists
6. Live event capacity limits
7. Search and filters
8. Downloads
9. Administrator moderation (remove comment/content, ban)
10. Persistence round-trip (including LiveEvent transient-field reload safety)

Run:

```bash
./mvnw compile exec:java -Dexec.mainClass="stemplatform.stem.IntegrationDemo"
```

Or run `IntegrationDemo.main` from your IDE. Exit code `1` if any check fails.

---

## Design Patterns & Principles

| Pattern / principle | Where it appears |
|---------------------|------------------|
| **Inheritance / polymorphism** | `User` and `Content` hierarchies; persisted and used via base types |
| **Composition root** | `AppContext` constructs and shares services |
| **Service layer** | Auth, content, download, notification, search |
| **Strategy-like interfaces** | `Downloadable`, `Streamable` |
| **Observer-style fan-out** | Creators’ subscribers notified on publish |
| **Session object** | `StreamingSession` encapsulates playback state for one viewer/video pair |
| **Serializable aggregate** | `ApplicationState` as the unit of persistence |
| **Daemon workers** | Auto-save scheduler; download thread pool |
| **Fail-safe load** | Corrupt/missing `application.dat` → empty state |
| **UI kit / shared styling** | `UiKit` + CSS classes for consistent controls |

Validation is pushed into domain constructors and service methods (`IllegalArgumentException` / `IllegalStateException`) so invalid state is rejected early.

---

## Data Flow

### Login → dashboard

```
LoginView
  → AuthenticationService.login(email, password)
  → instanceof Viewer | Creator | Administrator
  → ViewerDashboardView / CreatorDashboardView / AdminDashboardView.build(...)
  → AppContext.setRoot(newRoot)
```

### Publish content (creator)

```
CreatorDashboardView (Publish form)
  → new Video | Article | ResearchPaper | Presentation
  → ContentManager.publishContent(creator, content)
  → NotificationService.notifySubscribers(content)   [when wired from UI]
  → AppContext.save() (periodic or on exit)
```

### Watch video (viewer)

```
Viewer.watch(video)
  → new StreamingSession(viewer, video)
  → session.start()
      → video.incrementViewCount()
      → library.addToHistory(video)
  → pause / resume / seek / stop as needed
```

### Download (viewer)

```
DownloadManager.downloadAsync(downloadable, dest, onSuccess, onFailure)
  → background Files.copy(source → dest)
  → append to downloadHistory
  → callback (wrap UI updates in Platform.runLater)
```

### Persist

```
In-memory ApplicationState (users, content, events)
  → FileManager.save → data/application.dat
  → FileManager.load on next launch
```

---

## License / Notes

- This repository is an educational STEM platform project (Maven artifact version `1.0-SNAPSHOT`).
- Persistence uses Java serialization for simplicity; schema evolution and concurrent multi-user server use are out of scope.
- Passwords are not hashed; treat credentials as demo-only.
- Some JavaFX ecosystem libraries are declared in `pom.xml` for tooling/templates; the primary UI is code-constructed JavaFX with custom CSS rather than a heavy FXML-driven layout.

---

**EduSphere** — *Learn. Create. Share.*
