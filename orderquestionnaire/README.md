# OrderQuestionnaire

Aplicação de gerenciamento de questionários com arquitetura hexagonal. Implementa uma plataforma de coleta de respostas estruturadas com validação de respostas em tempo real, suportando regras complexas de visibilidade e formato de resposta por canal e jornada.

## Estrutura Modular

```
orderquestionnaire/
├── modules/
│   ├── domain/          # Regras de negócio puras (Question, Questionnaire, validação)
│   ├── application/     # Orquestração de casos de uso via pipeline (CreateQuestionnaire, ValidateAnswers, etc.)
│   ├── adapters/
│   │   ├── in/          # Adapters de entrada (REST, gRPC, SQS)
│   │   └── out/         # Adapters de saída (Mongo, API Distribution, Cloud-AWS)
│   └── bootstrap/       # Montador da aplicação Spring Boot
├── build.gradle         # Gradle parent
├── settings.gradle      # Gradle composição de módulos
└── README.md           # Este arquivo
```

## Documentação por Módulo

- **[modules/domain](modules/domain/README.md)**: Modelo de domínio, factories, validações
- **[modules/application](modules/application/README.md)**: Orquestração de casos de uso
- **[modules/adapters/in/api-rest](modules/adapters/in/api-rest/README.md)**: Endpoints REST
- **[modules/adapters/in/api-grpc](modules/adapters/in/api-grpc/README.md)**: Transporte gRPC
- **[modules/adapters/in/queue-sqs](modules/adapters/in/queue-sqs/README.md)**: Consumo de fila SQS
- **[modules/adapters/out/mongo](modules/adapters/out/mongo/README.md)**: Persistência MongoDB
- **[modules/adapters/out/api-distribution](modules/adapters/out/api-distribution/README.md)**: Integração com distribuidor de canais
- **[modules/adapters/out/cloud-aws](modules/adapters/out/cloud-aws/README.md)**: Publicação de eventos em SQS/SNS
- **[modules/bootstrap](modules/bootstrap/README.md)**: Configuração Spring Boot e startup

## Começando Localmente

### Pré-requisitos

- Java 25 (via Gradle toolchain)
- Docker + Docker Compose (para infraestrutura local)
- Gradle (incluído via gradlew)

### Rodar a Infraestrutura

```bash
cd ../..
docker compose -f docker/docker-compose.yml up -d
```

Isso inicia MongoDB, observabilidade (OTEL, Prometheus, Loki, Grafana), Wiremock (simulador de APIs) e MiniStack (SQS/SNS).

### Rodar os Testes

```bash
# Unit tests apenas
./gradlew :orderquestionnaire:test

# Ou do root
./gradlew :orderquestionnaire:modules:domain:test
./gradlew :orderquestionnaire:modules:application:test
./gradlew :orderquestionnaire:modules:adapters:in:api-rest:test
```

### Rodar a Aplicação

#### Via Gradle

```bash
./gradlew :orderquestionnaire:modules:bootstrap:bootRun
```

#### Via IDE

Abra `runs/orderquestionnaire[standalone execution].run.xml` no seu IDE (IntelliJ, VS Code com extensão Gradle).

## Endpoint Principal: Validação de Respostas

Valida um conjunto de respostas contra um questionário cadastrado, retornando um relatório detalhado de violações.

### Request

```http
POST /api/v1/questionnaires/validate-answers
Content-Type: application/json

{
  "questionnaireId": "q_satisfaction",
  "channelDistributionId": "app_mobile",
  "journeyDistributionId": "journey_feedback",
  "answers": {
    "q_satisfaction_level": 8,
    "q_recommend": "yes"
  }
}
```

### Response (200 - Valid)

```json
{
  "data": {
    "questionnaireId": "q_satisfaction",
    "channelDistributionId": "app_mobile",
    "journeyDistributionId": "journey_feedback",
    "valid": true,
    "violationsByQuestionId": {}
  }
}
```

### Response (200 - Invalid)

```json
{
  "data": {
    "questionnaireId": "q_satisfaction",
    "channelDistributionId": "app_mobile",
    "journeyDistributionId": "journey_feedback",
    "valid": false,
    "violationsByQuestionId": {
      "q_satisfaction_level": {
        "questionId": "q_satisfaction_level",
        "questionLabel": "How satisfied are you?",
        "order": 1,
        "providedAnswer": 15,
        "violations": [
          {
            "code": "ANSWER_OUT_OF_RANGE",
            "message": "Value must be between 0 and 10",
            "source": "ANSWER_CONFIGURATION",
            "ruleType": "NUMBER_RANGE",
            "ruleAttributes": {"min": 0, "max": 10, "providedValue": 15},
            "rulePath": "answerConfiguration"
          }
        ]
      }
    }
  }
}
```

### Exemplos Completos

Veja a coleção de API em [`api_collection/OpenAPI definition/Questionnaire/`](../../../api_collection/OpenAPI%20definition/Questionnaire/):

- [Validate questionnaire answers.yml](../../../api_collection/OpenAPI%20definition/Questionnaire/Validate%20questionnaire%20answers.yml)

## Fluxo de Validação

1. **Request** → controller REST traduz HTTP para `ValidateQuestionnaireAnswersCommand`
2. **Use Case** → pipeline orquestra 3 steps:
   - `ValidateQuestionnaireAnswersCommandStep`: valida command
   - `FetchQuestionnaireForAnswersValidationStep`: recupera questionário ou falha com `QUESTIONNAIRE_NOT_FOUND`
   - `ValidateAnswersAgainstQuestionnaireStep`: executa validação de domínio, mapeia violações
3. **Result** → `Result<ValidateQuestionnaireAnswersView, List<DomainError>>`
   - Se sucesso → HTTP 200 com `valid` flag (true ou false)
   - Se erro de domínio → HTTP 404/400 via exception handler

## Propriedades de Configuração

Em `modules/bootstrap/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/orderquestionnaire

question:
  pipeline:
    create:
      enabled: true
      order: [command_step, persistence_step]
    # ...

questionnaire:
  pipeline:
    validate-answers:
      enabled: true
      order: [command_step, fetch_step, validate_step]
    # ...

otel:
  exporter:
    otlp:
      endpoint: http://localhost:4317
```

## Observabilidade

A aplicação expõe métricas e logs estruturados:

- **Actuator**: `/actuator/health`, `/actuator/prometheus`
- **Logs**: JSON + Loki stack (veja [`docker/loki/`](../../../docker/loki/))
- **Traces**: OTEL Collector → Tempo

Acesso Grafana: http://localhost:3000

## Relacionamento com Outros Módulos Root

- **`shared`**: padrões `Result<V,E>`, `DomainError`, `Guard`, `@UseCase`, etc.
- **`observability`**: `@Loggable`, pipeline de logging e tracing
- **`security-server`**: autenticação/autorização (não integrado por padrão neste template)

Ver [`README.md` raiz](.../README.md).

