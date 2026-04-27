# 🦁 Royal Classico FC — Official Club Website

> **EST. 2026** — Official website of Royal Classico Football Club, an amateur football club portfolio built with Spring Boot + Thymeleaf + MongoDB.

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Secret Admin Access](#secret-admin-access)
- [API Reference](#api-reference)
- [Image Management](#image-management)
- [Project Structure](#project-structure)

---

## Project Overview

Royal Classico FC is a fully-featured amateur football club website with:

- **Public pages** — Home (hero slider + news), Squad, News archive
- **Live fixture banner** — Countdown timer to the next match
- **Secret admin API** — Token-protected REST API for managing players, news, and fixtures
- **No public login page** — Security through obscurity: wrong token returns HTTP 404

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring MVC |
| Database | MongoDB (via Spring Data) |
| Templating | Thymeleaf |
| Frontend | Vanilla CSS + JS (Royal Blue + Antique Gold theme) |
| Fonts | Playfair Display, Montserrat, Bebas Neue |
| File storage | Local filesystem (`./uploads/`) |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MongoDB running on `localhost:27017`

### Run

```bash
# Clone and enter the project
cd royal-classico

# Run with Maven
./mvnw spring-boot:run

# Or set a custom admin secret at runtime
SYSTEM_ADMIN_KEY=my-secret ./mvnw spring-boot:run
```

The site will be available at **http://localhost:8080**

---

## Configuration

All settings live in `src/main/resources/application.properties`:

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/royal_classico_db

# Upload directory (where player/news images are stored)
app.upload.dir=./uploads

# Admin secret — CHANGE THIS IN PRODUCTION
app.admin.secret=RC-ADMIN-XKDF92-CLASSIFIED

# Max upload size
spring.servlet.multipart.max-file-size=10MB
```

> **Production tip:** Override `app.admin.secret` via the `SYSTEM_ADMIN_KEY` environment variable rather than changing the properties file.

---

## Secret Admin Access

There is **no public login page** — this is intentional ("Security through Obscurity"). All admin write operations are protected by the `X-Admin-Secret` HTTP header.

### Activating Admin Mode (Browser Console)

1. Open **http://localhost:8080** in your browser.
2. Open DevTools → **Console** tab (`F12`).
3. Paste and run:

```javascript
localStorage.setItem('admin_secret_token', 'RC-ADMIN-XKDF92-CLASSIFIED');
location.reload();
```

4. The page reloads. A gold **⚙ ADMIN** badge appears in the navbar.
5. Admin-only UI elements (delete buttons, add forms) become visible.

### Deactivating Admin Mode

```javascript
localStorage.removeItem('admin_secret_token');
location.reload();
```

Or click the **⚙ ADMIN** badge in the navbar.

### How It Works

The `admin.js` script (loaded on every page) reads `admin_secret_token` from `localStorage`. If present, it:

1. Patches `window.fetch()` to inject `X-Admin-Secret: <token>` on all requests to `/api/v1/rc-management-internal/*`
2. Shows all `[data-admin-only]` elements in the DOM
3. Exposes `window.RC.api()` helper for console-driven admin operations

### Console API Helpers

Once admin mode is active, `RC.api()` is available:

```javascript
// List all players
RC.api('GET', '/players').then(console.log)

// Delete a player by ID
RC.api('DELETE', '/players/64f2a...')

// Delete a news post
RC.api('DELETE', '/news/64f2b...')

// Add a fixture (JSON body)
RC.api('POST', '/fixtures', {
  opponent: "FC Rivals",
  matchDate: "2026-05-15",
  matchTime: "18:00",
  pitch: "Main Stadium"
})
```

---

## API Reference

**Base URL:** `/api/v1/rc-management-internal`  
**Auth:** `X-Admin-Secret: <your-secret>` header required on all requests.  
**Security:** Missing or wrong header → `HTTP 404` (not 401/403).

### Players

| Method | Path | Description |
|---|---|---|
| `GET` | `/players` | List all players |
| `POST` | `/players` | Add a player (multipart/form-data) |
| `PUT` | `/players/{id}` | Update a player |
| `DELETE` | `/players/{id}` | Delete a player |

**POST/PUT fields:**
- `name` — Player full name
- `position` — `GK` / `DEF` / `MID` / `FWD`
- `jerseyNumber` — Integer 1–99
- `image` *(optional)* — Player photo (JPG/PNG, max 10MB)

### News Posts

| Method | Path | Description |
|---|---|---|
| `GET` | `/news` | List all news posts |
| `POST` | `/news` | Create a post (multipart/form-data) |
| `PUT` | `/news/{id}` | Update a post |
| `DELETE` | `/news/{id}` | Delete a post |

**POST/PUT fields:**
- `title` — Article headline
- `content` — Article body text
- `image` *(optional)* — Cover image (JPG/PNG, max 10MB)

### Fixtures

| Method | Path | Description |
|---|---|---|
| `GET` | `/fixtures` | List all fixtures |
| `POST` | `/fixtures` | Add a fixture |
| `PUT` | `/fixtures/{id}` | Update a fixture |
| `DELETE` | `/fixtures/{id}` | Delete a fixture |

---

## Image Management

Images are stored on the **local filesystem** under the `./uploads/` directory (configurable via `app.upload.dir`).

### Automatic Cleanup

> **Old images are deleted automatically.** When you update a player or news post with a new image, the previous image file is permanently deleted from disk before the new one is saved. No manual cleanup is needed.

### Upload Path

Uploaded files are served at:
```
http://localhost:8080/uploads/<filename>
```

### Supported Formats

- JPEG / JPG
- PNG
- WebP

Maximum file size: **10 MB** per upload.

### Backup Advice

Before deploying to production, backup the `./uploads/` directory separately — it is **not** stored in MongoDB, only the filename is.

---

## Project Structure

```
royal-classico/
├── src/main/
│   ├── java/com/royalclassico/
│   │   ├── config/
│   │   │   └── WebMvcConfig.java          # Static resource & upload URL config
│   │   ├── controller/
│   │   │   ├── AdminFixtureController.java
│   │   │   ├── AdminManagementController.java  # Main admin REST API
│   │   │   ├── AdminNewsController.java
│   │   │   ├── AdminPlayerController.java
│   │   │   ├── PublicApiController.java
│   │   │   └── PublicPageController.java   # Serves Thymeleaf pages
│   │   ├── model/
│   │   ├── repository/
│   │   ├── security/
│   │   │   └── AdminSecurityFilter.java   # X-Admin-Secret enforcement
│   │   └── service/
│   └── resources/
│       ├── static/
│       │   ├── css/style.css              # Royal Blue + Antique Gold theme
│       │   ├── js/
│       │   │   ├── main.js                # Slider, countdown, navbar scroll
│       │   │   └── admin.js               # Admin SDK (localStorage → fetch header)
│       │   └── images/
│       │       └── logo.svg               # Heraldic shield crest
│       ├── templates/
│       │   ├── index.html
│       │   ├── squad.html
│       │   └── news.html
│       └── application.properties
├── uploads/                               # Runtime image storage (gitignored)
├── pom.xml
└── README.md
```

---

## Brand Identity

| Token | Value | Usage |
|---|---|---|
| Royal Blue | `#003366` | Primary / navbar background |
| Deep Navy | `#0A1128` | Page background |
| Antique Gold | `#C5A065` | Borders, accents, highlights |
| Highlight Gold | `#E8D1A3` | Hover states, light accents |
| Text Light | `#F4F4F4` | Body text |

**Fonts:** Playfair Display (headlines) · Montserrat (body) · Bebas Neue (display/scores)

---

*Royal Classico Football Club — Passion. Unity. Victory. — EST. 2026*
