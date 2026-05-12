# API-REST Adapter - OrderQuestionnaire

Exposição de use cases via HTTP/JSON. Traduz requests HTTP para commands/queries, executa via application layer, e mapeia resultados para responses estruturadas.

## Arquitetura

```
api-rest/
├── questionnaire/
│   ├── entrypoint/          # Controller + API interface (contrato OpenAPI)
│   ├── request/             # DTOs de entrada (ValidateQuestionnaireAnswersRequest, etc.)
│   └── response/            # DTOs de saída (ValidateQuestionnaireAnswersResponse, etc.)
├── question/
│   ├── entrypoint/
│   ├── request/
│   └── response/
├── common/                  # ApiDataResponse, ApiCollectionResponse, ApiMeta, ApiLinks
├── error/                   # GlobalExceptionHandler, ProblemDetailsFactory, ApiErrorCodes
├── filters/                 # RestHeadersFilter (tenant context, correlation IDs)
└── audit/
    ├── request/             # AuditUserRequest
    └── response/            # AuditUserResponse
```

## Endpoints Principais

### POST /api/v1/questionnaires/validate-answers

Valida respostas contra um questionário.

**Request**:
```json
{
  "questionnaireId": "q_001",
  "channelDistributionId": "mobile_app",
  "journeyDistributionId": "journey_01",
  "answers": {"q_age": 25}
}
```

**Response (200 - Valid)**:
```json
{
  "data": {
    "questionnaireId": "q_001",
    "channelDistributionId": "mobile_app",
    "journeyDistributionId": "journey_01",
    "valid": true,
    "violationsByQuestionId": {}
  }
}
```

**Response (200 - Invalid)**:
```json
{
  "data": {
    "valid": false,
    "violationsByQuestionId": {
      "q_age": {
        "questionId": "q_age",
        "questionLabel": "Age",
        "order": 1,
        "providedAnswer": -5,
        "violations": [
          {
            "code": "ANSWER_OUT_OF_RANGE",
            "message": "Value must be between 0 and 120",
            "source": "ANSWER_CONFIGURATION",
            "ruleType": "NUMBER_RANGE",
            "ruleAttributes": {"min": 0, "max": 120},
            "rulePath": "answerConfiguration"
          }
        ]
      }
    }
  }
}
```

### POST /api/v1/questionnaires

Cria novo questionário.

### GET /api/v1/questionnaires/{id}

Recupera questionário por composite key (id + channelDistributionId + journeyDistributionId).

### Mais

PUT /api/v1/questionnaires/{id}, DELETE /api/v1/questionnaires/{id}, DELETE /api/v1/questionnaires (batch), GET /api/v1/questionnaires (search página/cursor)

Análogo para `/api/v1/questions`.

## Error Handling

Classe `GlobalExceptionHandler` traduz exceções para `ProblemDetail` (RFC 7807):

- `DomainResultException` → `400/404/409` baseado em código de erro
- `MethodArgumentNotValidException` → `400` (validação de request)
- `HttpMessageNotReadableException` → `400` (payload malformado)
- Exception genérica → `500`

```yaml
{
  "type": "about:blank",
  "title": "Resource not found",
  "status": 404,
  "code": "DOM-013",
  "detail": "questionnaire 'xyz' was not found",
  "instance": "/api/v1/questionnaires/validate-answers",
  "errors": [...]
}
```

## Request/Response Patterns

### ApiDataResponse (sucesso com dados)

```java
{
  "data": <T>,
  "meta": {...},  // opcional
  "links": {...}  // opcional
}
```

### ApiCollectionResponse (sucesso com lista)

```java
{
  "data": [<T>, ...],
  "meta": {
    "mode": "PAGE",
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "hasNext": true,
    "nextCursor": null
  },
  "links": {
    "next": "...",
    "prev": "..."
  }
}
```

## CORS Configuration

Cada endpoint pode ter CORS customizado via `@CrossOrigin`:

```java
@CrossOrigin(
    originPatterns = "${...allowed-origin-pattern:http://localhost:*}",
    allowedHeaders = "*",
    exposedHeaders = {HeaderConstants.CORRELATION_HEADER, HeaderConstants.FLOW_HEADER},
    methods = RequestMethod.POST,
    maxAge = 3600
)
```

Propriedades em `application.yml`:
```yaml
orderquestionnaire:
  adapters:
    in:
      api-rest:
        cors:
          questionnaire:
            validate-answers:
              allowed-origin-pattern: http://localhost:*
```

## Headers Padrão

- `X-Correlation-Id`: rastreamento end-to-end
- `X-Flow-Id`: fluxo de negócio
- `X-Tenant-Id`: identificação de tenant (if applicable)

Gerenciados por `RestHeadersFilter` (scaffold).

## Testes

- **Unit**: `QuestionnaireApiIntegrationTest` (Mock MVC, validação de contrato)
- **Integration**: Testcontainers + MockMvc (banco real, adapters reais)

## Documentação

Endpoints documentados via `@Operation`, `@ApiResponse`, `@Schema` do SpringDoc. Acessível em:
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- OpenAPI YAML: `/v3/api-docs.yaml`

Coleta de exemplos em [`api_collection/OpenAPI definition/`](../../../../api_collection/OpenAPI%20definition/).

