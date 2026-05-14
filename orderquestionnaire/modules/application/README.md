# Application Module - OrderQuestionnaire

Orquestração de casos de uso via padrão pipeline. Encapsula a lógica de fluxo de negócio, séries de validações e transformações de dados, sem dependências de frameworks web ou persistência.

## Arquitetura

```
application/
├── questionnaire/
│   ├── dto/
│   │   ├── command/          # ValidateQuestionnaireAnswersCommand, CreateQuestionnaireCommand, etc.
│   │   ├── view/             # ValidateQuestionnaireAnswersView, QuestionnaireView, etc.
│   │   └── queries/          # GetQuestionnaireById, SearchQuestionnaireByFilter
│   ├── port/
│   │   ├── in/               # Interfaces de entrada (use cases): ValidateQuestionnaireAnswersUseCase, etc.
│   │   └── out/              # Interfaces de saída (repositories, adaptadores): QuestionnaireCommandOutPort, etc.
│   ├── service/
│   │   ├── step/             # Pipeline steps: ValidateAnswersAgainstQuestionnaireStep, FetchQuestionnaireForAnswersValidationStep
│   │   └── context/          # Contexto do pipeline: ValidateQuestionnaireAnswersPipelineContext
│   └── error/                # Enums de erro: QuestionnaireErrors
├── question/
│   └── ...                   # Similar à estrutura de questionnaire
├── common/                   # QueryHandler, PipelineOrchestrator
└── ...
```

## Casos de Uso Principais

### ValidateQuestionnaireAnswersUseCase

Valida as respostas de um usuário contra um questionário armazenado.

**Entrada**: `ValidateQuestionnaireAnswersCommand` com `questionnaireId`, `channelDistributionId`, `journeyDistributionId`, `answers: Map<String, Object>`

**Saída**: `ValidateQuestionnaireAnswersView` com `valid: Boolean` e `violationsByQuestionId: Map<String, QuestionAnswerValidationView>`

**Pipeline Steps** (em ordem):
1. `ValidateQuestionnaireAnswersCommandStep`: valida estrutura básica do command
2. `FetchQuestionnaireForAnswersValidationStep`: recupera o questionário via port outbound ou falha com `QUESTIONNAIRE_NOT_FOUND`
3. `ValidateAnswersAgainstQuestionnaireStep`: chama `questionnaire.answerValidation(answers)` do domínio, mapeia violações para view

### Outros Casos de Uso

- `CreateQuestionnaireUseCase`: cria novo questionário
- `UpdateQuestionnaireUseCase`: atualiza questionário
- `DeleteQuestionnaireUseCase`: delete com batch support
- `CreateQuestionUseCase`: cria pergunta catalogo
- `UpdateQuestionUseCase`: atualiza pergunta
- `DeleteQuestionUseCase`: delete com batch support

## Padrão Pipeline

Herda de `PipelineOrchestrator<Context, Result>`. Cada use case é uma série de `Step<Context>` que:

1. Recebem contexto
2. Executam lógica
3. Armazenam resultado ou erro no contexto
4. Retornam `Result<Void, List<DomainError>>`

```java
public class ValidateQuestionnaireAnswersService
    extends PipelineOrchestrator<ValidateQuestionnaireAnswersPipelineContext, ValidateQuestionnaireAnswersView>
    implements ValidateQuestionnaireAnswersUseCase {
    // ...
}
```

## Ports (Contrato com Fora)

### Entrada (In Ports)
- Qualquer `UseCase` interface é um in port (ex.: `ValidateQuestionnaireAnswersUseCase`)
- QueryHandlers são também in ports: `GetQuestionnaireById`, `SearchQuestionnaireByFilter`

### Saída (Out Ports)
- `QuestionnaireCommandOutPort`: persistência (find, save, delete, etc.)
- `QuestionQueryOutPort`: queries read-only

## Mapeamento de Erros

Enum `QuestionnaireErrors` mapeia códigos de domínio para mensagens:

- `QUESTIONNAIRE_NOT_FOUND`
- `CHANNEL_NOT_VALID`
- etc.

Cada erro pode ser convertido para `DomainError` via `QuestionnaireErrors.CODE.toDomainError()`.

## Testes

- **Unit**: `ValidateQuestionnaireAnswersServiceTest` (pipeline completo com mocks)
- **BDD**: `ValidateQuestionnaireAnswersStepDefs` (Cucumber, fluxos end-to-end)
- **Functional**: `FunctionalTest` (sem Spring, testes de mutação com PIT)

Cobertura esperada: **100% de linhas e branches** (jacoco).

## Notas

- **Zero dependências externas**: apenas `shared` e `domain`
- **Padrão funcional**: força resultado explícito, sem exceções de negócio
- **Contexto thread-safe**: cada pipeline instância é isolada para seu request

