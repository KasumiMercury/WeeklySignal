# Repository Guidelines

## Project Structure & Module Organization
- Root Gradle build with Kotlin Multiplatform. Main module: `composeApp`.
- Source sets: `composeApp/src/commonMain`, `androidMain`, `desktopMain`; tests in `commonTest`.
- Resources: `commonMain/composeResources` (shared), `androidMain/res` (Android XML).
- Android entry: `net.mercuryksm.MainActivity`; Desktop entry: `net.mercuryksm.MainKt`.

## Build, Test, and Development Commands
- Build all: `./gradlew build` — compiles all targets and runs unit tests.
- Desktop run: `./gradlew :composeApp:run` — launches the Compose Desktop app.
- Android debug APK: `./gradlew :composeApp:assembleDebug`; install: `./gradlew :composeApp:installDebug`.
- Tests (all): `./gradlew :composeApp:allTests`; Android unit tests: `./gradlew :composeApp:testDebugUnitTest`.
- License report: `./gradlew :composeApp:licenseReport` — generates reports under `composeApp/build/reports/dependency-license`.

## Coding Style & Naming Conventions
- Kotlin official style, 4‑space indentation, no tabs. Organize imports.
- Packages prefixed `net.mercuryksm`. Classes/objects: PascalCase; functions/vars: camelCase; constants: UPPER_SNAKE_CASE.
- Compose screens end with `Screen` (e.g., `ExportImportScreen`); view models end with `ViewModel`.
- Keep platform code in the correct source set; shared logic in `commonMain`.

## Testing Guidelines
- Framework: `kotlin.test` in `composeApp/src/commonTest`.
- Name tests descriptively; Kotlin backticked names encouraged (see `EntityMappersTest`).
- Add tests for new business logic, mappers, and edge cases. Run with `./gradlew test` or the tasks above.

## Commit & Pull Request Guidelines
- Use Conventional Commits: `feat:`, `fix:`, `chore:`, `refactor:`, etc. Example: `fix: adjust time slot generation to skip to next item`.
- PRs should include: clear description, linked issue (e.g., `#43`), screenshots/GIFs for UI changes, and test notes.
- Keep changes scoped to one concern; update docs when behavior or commands change.

## Security & Configuration Tips
- Do not commit secrets. Local overrides live in `local.properties` (ignored).
- Room schemas in `composeApp/schemas`; keep updated when changing entities.
