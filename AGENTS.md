# AGENTS.md

## Purpose
- This repo is a **hexagonal architecture playground/template**: prioritize clean boundaries over feature completeness.
- Active implementation focus is strongest in `orderquestionnaire/modules/domain` and `orderquestionnaire/modules/application`.

## Architecture Map (What talks to what)
- Boot app for OrderQuestionnaire lives in `orderquestionnaire/modules/bootstrap` (`OrderQuestionnaireApplication`) and scans `com.acme.orderquestionnaire`.
- Domain layer: root `modules/domain/*` plus `orderquestionnaire/modules/domain` hold business rules and factories (no Spring dependency expected).
- Application layer: root `modules/application/*` plus `orderquestionnaire/modules/application` orchestrate use cases and ports.
- Inbound adapters (OrderQuestionnaire source tree): `orderquestionnaire/modules/adapters/in/api-rest`, `orderquestionnaire/modules/adapters/in/api-grpc`, `orderquestionnaire/modules/adapters/in/queue-sqs`.
- Outbound adapters (OrderQuestionnaire source tree): `orderquestionnaire/modules/adapters/out/mongo`, `orderquestionnaire/modules/adapters/out/api-distribution`, `orderquestionnaire/modules/adapters/out/cloud-aws`.
- Cross-cutting: `shared` (Result/Guard/stereotypes/tenant/header constants) is consumed as a composite build, plus root `observability`; security is wired via composite build `security-server`.
- Root `settings.gradle` currently includes composite builds `shared`, `observability`, `security-server`, and `orderquestionnaire`; root task paths for those contexts are prefixed with `:shared:`, `:observability:`, `:security-server:`, and `:orderquestionnaire:`.
- Inside `orderquestionnaire/settings.gradle`, `:modules:domain`, `:modules:application`, `:modules:adapters:out:mongo`, `:modules:adapters:out:api-distribution`, `:modules:adapters:in:api-rest`, and `:modules:bootstrap` are currently included; `shared` is consumed via `includeBuild('../shared')`, and observability is consumed via `includeBuild('../observability') { name = 'observability-lib' }`, while `api-grpc`, `queue-sqs`, and `out/cloud-aws` still exist in source tree but are not active Gradle projects there.

## Project Conventions (specific to this repo)
- Use functional flow with `Result<V,E>` + `Guard` instead of throwing for business validation (`shared/.../result`).
- Use architectural annotations from shared stereotypes (`@UseCase`, `@OutputPort`, etc.) to mark intent.
- Domain factories return builders wrapped in `Result` and accumulate errors (see `QuestionFactory`, `QuestionnaireFactory`).
- Header names are standardized in `shared/src/main/java/com/acme/shared/constants/HeaderConstants.java`.
- Tenant context is thread-local via `TenantContextHolder`; callers must set and clear it at request boundaries.
- OrderQuestionnaire application services are pipeline-based: use cases typically extend `PipelineOrchestrator`, work with `*Step` + `*PipelineContext`, and are assembled in bootstrap configs such as `orderquestionnaire/modules/bootstrap/src/main/java/com/acme/orderquestionnaire/config/pipeline/CreateQuestionnaireUseCaseConfig.java`.
- Pipeline step enablement/order is configuration-driven in `orderquestionnaire/modules/bootstrap/src/main/resources/application.yml` under `question.pipeline.*` and `questionnaire.pipeline.*`.
- ArchUnit tests actively enforce naming and boundary conventions in `orderquestionnaire/modules/application/src/test/java/com/acme/orderquestionnaire/application/architecture/HexagonalArchitectureModuleTest.java` and `orderquestionnaire/modules/domain/src/test/java/com/acme/orderquestionnaire/domain/architecture/HexagonalArchitectureModuleTest.java`.
- For detailed shared conventions, use `shared/README.md` and `shared/docs/*.md` (`ResultPattern.md`, `RuleEngine.md`, `Stereotypes.md`, `TenantHeaders.md`, `Pagination.md`, `StateMachine.md`, `Pipeline.md`, `ValueObjectsEnums.md`).
- Agent guidance files currently discovered by convention glob are root `AGENTS.md` and `README.md`, `shared/README.md`, `purefilter/README.md`, `modules/domain/*/README.md`, `orderquestionnaire/modules/**/README.md`, `security-server/README.md` plus `security-server/modules/**/README.md`, and `docker/aws-manager/README.md`; no `.github/copilot-instructions.md` / `.cursor` / `.windsurf` / `.clinerules` instruction sets are present.

## Developer Workflow
- Unit tests: `./gradlew clean test` (Windows: `.\gradlew.bat clean test`).
- Aggregate coverage for Sonar: `./gradlew clean aggregateCoverageReport sonarqube`.
- Domain mutation tests (orderquestionnaire composite build): `./gradlew :orderquestionnaire:modules:domain:pitest`.
- Build shared standalone/composite from root: `./gradlew :shared:build`.
- Build observability standalone/composite from root: `./gradlew :observability:build`.
- Windows equivalents for common advanced tasks: `.\gradlew.bat clean aggregateCoverageReport sonarqube` and `.\gradlew.bat :orderquestionnaire:modules:domain:pitest`.
- Windows shared build from root: `.\gradlew.bat :shared:build`.
- Windows observability build from root: `.\gradlew.bat :observability:build`.
- Build uses Java toolchain `25` (`build.gradle`); align IDE/Gradle JVM before running tasks.
- Local infra stack: `docker compose -f docker/docker-compose.yml up -d` (down with `down -v`).
- Predefined IDE runs live in `runs/*.run.xml` (unit, integration, coverage, Sonar, message-manager).

## Integration/Runtime Notes
- Observability stack in compose: OTEL Collector, Prometheus, Tempo, Loki, Promtail, Grafana.
- Compose also starts `mongo-express` (port `8081`) and `stackport` UI/API (host port `5000`) for local queue/topic inspection against MiniStack.
- Compose also starts `wiremock` (host port `8082`); `orderquestionnaire/modules/bootstrap/src/main/resources/application.yml` points channel/journey distribution API base URLs there by default.
- Boot config exposes actuator `health,info,prometheus` and OTLP endpoint via `OTEL_EXPORTER_OTLP_ENDPOINT` (`application.yml`).
- Logs are JSON via `orderquestionnaire/modules/bootstrap/src/main/resources/logback-spring.xml` + `modules/observability/LoggingAspect.java` (`@Loggable`).
- Local AWS emulation uses MiniStack (`sqs,sns` via port `4566`); `docker/aws-manager` is present but its service is commented out in compose.

## Known Repository State (important before changing code)
- Several adapters/features are scaffolded or commented out (examples: `PersonController`, `RestHeadersFilter`, gRPC interceptor, mongo config).
- `orderquestionnaire/modules/adapters/in/api-grpc`, `orderquestionnaire/modules/adapters/in/queue-sqs`, and `orderquestionnaire/modules/adapters/out/cloud-aws` are scaffold-oriented directories (`README.md`/`docs`/`src`) and currently do not have their own `build.gradle`.
- `orderquestionnaire/modules/adapters/in/api-rest` is currently included in `orderquestionnaire/settings.gradle` (`:modules:adapters:in:api-rest`) and has its own `build.gradle`.
- Some docs/configs still reference legacy paths (`modules/adapters/in/*`, `modules/shared`, `modules/security`) while active Gradle modules use `orderquestionnaire/modules/adapters/*`, composite build `shared`, and composite build `security-server`.
- `Taskfile.yml` has working `build`/`test`/`up`/`down` shortcuts, but `docker:build` still points to legacy root `modules/bootstrap`; prefer service-specific bootstrap directories when building images.
- `scripts/*.sh` and `scripts/*.bat` exist but are empty; prefer `Taskfile.yml`, `makefile`, or direct Gradle/Docker commands.
- `runs/security-server[integration test].run.xml` and `makefile` target integration tasks (`integrationTest`), and `runs/security-server[integration coverage].run.xml` calls `:modules:bootstrap:jacocoIntegrationTestReport`; these tasks are not explicitly declared in current Gradle scripts.
- `runs/Run Message Manager.run.xml` points to `docker/message-manager`; current compose/runtime assets are `docker/stackport` (active) and `docker/aws-manager` (present but commented in compose).
- Root `clean test` currently fails with a circular task dependency at `:modules:application:personmdm`; for orderquestionnaire changes, use focused composite tasks (for example `:orderquestionnaire:modules:domain:test`).
- `purefilter` is a standalone Gradle build in the workspace (`purefilter/settings.gradle`) but is not included from root `settings.gradle`; root Gradle tasks do not cover it.
- `docker/docker-compose.yml` provides an optional `app` container under profile `app-container`; default local flow still runs apps from IDE/Gradle unless that profile is explicitly enabled.
- If adding production behavior, verify whether module is active or intentionally stubbed before wiring dependencies.

## Safe Change Strategy for Agents
- Keep new business rules in domain factories/entities; keep orchestration in application services.
- Introduce ports in application first, then implement adapters; avoid leaking framework classes into domain.
- Reuse `Result`, `DomainError`, and shared constants to match existing patterns.
- When touching observability or headers, mirror behavior across REST/gRPC adapters where code is active.
