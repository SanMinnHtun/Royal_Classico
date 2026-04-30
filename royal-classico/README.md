# Royal Classico — Admin & Management README

This document explains the hidden admin UI, security headers, file upload locations, and quick startup notes.

## Hidden Gate & Admin Dashboard

- Obscured Admin Dashboard URL (UI):
  - /royal-classico/admin-control-system-april202650

- Entrance page (UI):
  - /royal-classico/entrance — simple PIN entry. When the correct PIN is entered the browser stores the admin secret in localStorage and redirects to the dashboard.

- PIN for the local hidden gate (development): `20526`.
  - When correct, client JS will run:
    - localStorage.setItem('X-Admin-Secret', 'RC_SECRET_2026')
    - window.location.href = '/royal-classico/admin-control-system-april202650'

## Hardened API & Header Security

- All internal management REST endpoints are mounted under:
  - `/api/v1/management-internal/**`

- Protected header:
  - `X-Admin-Secret: RC_SECRET_2026`

- Server-side enforcement:
  - `AdminSecurityFilter` reads `app.admin.secret` from `application.properties`. If the header is missing or incorrect, the filter responds with HTTP 404 to avoid revealing the admin API.

- Client-side:
  - The dashboard code includes `X-Admin-Secret` from `localStorage.getItem('X-Admin-Secret')` on every fetch.

## Uploads & Serving

- Upload directory (configured in `application.properties`): `app.upload.dir` (default `./uploads`).
- FileService stores files under this directory and returns a relative path such as `players/uuid.jpg` or `news/uuid.jpg`.
- `WebMvcConfig` maps `/uploads/**` and `/images/**` to the uploads directory so images can be served via `/images/<relative-path>`.

## Startup Behavior

- The application creates the following directories automatically at startup (via a CommandLineRunner):
  - `<app.upload.dir>/players`
  - `<app.upload.dir>/news`

- Ensure MongoDB is running (the app uses `spring.data.mongodb.uri` in `application.properties`).

## How to run (development)

1. Open the project in IntelliJ and run `RoyalClassicoApplication`.
2. Visit `http://localhost:8080/`.
3. Click the small settings/gear icon in the header, enter PIN `20526`.
4. You'll be redirected to the admin dashboard and localStorage will have the header value needed for API calls.

## Notes & Security

- For production, replace the admin secret and do not rely on obscure URL paths as the main security mechanism.
- Consider using proper authentication (OAuth2, basic auth, or session-based login) and HTTPS.
- Back up the `uploads/` directory — uploaded files are stored on disk, only the filename is stored in MongoDB.

---
Generated and updated by the maintenance script on 2026-04-29.

