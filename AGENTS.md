# Repository Guidelines

This repository is a multi-app workspace for a research achievement management system. It includes a Vue 3 front end, a Spring Boot backend, and a Strapi-based CMS/admin.

## Project Structure & Module Organization
- `research-management-system/`: Vue 3 + Vite front end (UI, routing, stores, API clients).
- `achmanager-backend/`: Spring Boot service (`src/main/java`, `src/main/resources`).
- `my-strapi-project/`: Strapi app with admin UI and content APIs.
- `docs/`: supporting documentation; see `PROJECT_SUMMARY.md` for a high-level overview.

## Build, Test, and Development Commands
Run commands from the relevant subproject directory.

- Front end (`research-management-system/`)
  - `npm install`
  - `npm run dev` (local dev server)
  - `npm run build` (production build)
  - `npm run lint` / `npm run type-check`
- Backend (`achmanager-backend/`)
  - `./mvnw spring-boot:run` (run service, Java 17)
  - `./mvnw test` (unit/integration tests)
  - `./mvnw package` (build jar)
- Strapi (`my-strapi-project/`)
  - `npm install`
  - `npm run develop` (admin + API dev)
  - `npm run build` / `npm run start`

## Coding Style & Naming Conventions
- Front end: follow ESLint rules in `research-management-system/eslint.config.js`; Vue single-file components use `PascalCase` names (multi-word names allowed).
- Backend: standard Java conventions (packages lower-case, classes `PascalCase`, methods/fields `camelCase`).
- Keep file and directory names descriptive; group feature-specific UI under `src/views/` and shared logic under `src/utils/` or `src/api/`.

## Testing Guidelines
- Backend tests run via `./mvnw test` (Spring Boot test stack).
- Front-end/Strapi automated tests are not configured yet; validate changes via `npm run dev` and basic UI checks.
- Name new backend tests with `*Test.java` and place them under `achmanager-backend/src/test/java`.

## Commit & Pull Request Guidelines
- Git history uses short summaries, often with a conventional prefix (e.g., `feat: ...`, `fix: ...`) and Chinese text is acceptable.
- Commits should be scoped to a single concern and use imperative phrasing.
- PRs should include: a clear description, linked issue (if any), and screenshots for UI changes. Note any config or migration steps.

## Configuration & Secrets
- Environment files live in `research-management-system/.env*`, `my-strapi-project/.env`, and `achmanager-backend/.env`.
- Use `.env.example` in `my-strapi-project/` as a template and avoid committing secrets.
