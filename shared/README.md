# Shared Module

Este modulo concentra contratos e utilitarios reutilizaveis em toda a arquitetura hexagonal.
A meta e manter regras transversais em um lugar unico, com baixo acoplamento.

## O que existe aqui

- Fluxo funcional de sucesso/erro com `Result<V, E>` e `DomainError`
- Validacao declarativa com `Guard` e `DomainRuleRunner`
- Rule engine (`Rule`, `GenericRule`, `RuleValidator`, `FailNotification`)
- Estereotipos arquiteturais (`@UseCase`, `@InputPort`, `@OutputPort`, etc.)
- Contratos de paginacao hibrida (page/cursor)
- Maquina de estados generica com guardas e acoes
- Pipeline com compensacao (`Step`, `PipelineContext`, `PipelineOrchestrator`, `RollbackStyle`)
- Contexto de tenant por thread (`TenantContextHolder`)
- Constantes de headers (`HeaderConstants`)
- Value objects e enums compartilhados (`Id`, `AuditInfo`, etc.)

## Guia de leitura recomendado

- [`docs/ResultPattern.md`](docs/ResultPattern.md)
- [`docs/RuleEngine.md`](docs/RuleEngine.md)
- [`docs/Stereotypes.md`](docs/Stereotypes.md)
- [`docs/TenantHeaders.md`](docs/TenantHeaders.md)
- [`docs/Pagination.md`](docs/Pagination.md)
- [`docs/StateMachine.md`](docs/StateMachine.md)
- [`docs/Pipeline.md`](docs/Pipeline.md)
- [`docs/ValueObjectsEnums.md`](docs/ValueObjectsEnums.md)

## Onde este modulo e usado

Exemplos ativos no workspace:

- Aplicacao (`Result` + `Guard`):
  - `orderquestionnaire/modules/application/src/main/java/com/acme/orderquestionnaire/application/questionnaire/service/CreateQuestionnaireService.java`
  - `orderquestionnaire/modules/application/src/main/java/com/acme/orderquestionnaire/application/question/service/CreateQuestionService.java`
- Dominio (`DomainRuleRunner`):
  - `orderquestionnaire/modules/domain/src/main/java/com/acme/orderquestionnaire/domain/questionnaire/QuestionnaireFactory.java`
  - `orderquestionnaire/modules/domain/src/main/java/com/acme/orderquestionnaire/domain/question/QuestionFactory.java`

## Build e testes

Use o wrapper Gradle da raiz do repositorio (`hex-arch-template/`):

```powershell
.\gradlew.bat :shared:clean :shared:build
.\gradlew.bat :shared:test
```

## Quando usar e quando evitar

- Use `Result` para erros esperados de negocio
- Use exceptions para falhas tecnicas inesperadas
- Use `Guard.collect` para validacoes acumuladas
- Use `DomainRuleRunner` para regras declarativas de dominio
- Use `TenantContextHolder` apenas na borda da requisicao e sempre limpe ao final

## Dica de onboarding

Ao implementar uma feature nova:

1. Comece no dominio com `Result` + regras.
2. Orquestre no application via ports e use cases.
3. Traduza para protocolo (REST/gRPC/queue) apenas nos adapters.
4. Reaproveite contratos do `shared` antes de criar utilitarios locais.
