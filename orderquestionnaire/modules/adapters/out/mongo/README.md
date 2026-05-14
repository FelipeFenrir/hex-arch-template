# MongoDB Adapter (Outbound) - OrderQuestionnaire

Persistência de Question e Questionnaire em MongoDB. Implementa `QuestionnaireCommandOutPort` e `QuestionQueryOutPort` (entre outras).

## Coleções

- `questions`: catalogo de perguntas
  - Índice: `{id: 1, tenantId: 1}` (unique)
- `questionnaires`: questionários por canal/jornada
  - Índice: `{id: 1, channelDistributionId: 1, journeyDistributionId: 1, tenantId: 1}` (unique)

## Repositories

- `QuestionRepositoryMongo`: implementa in/out ports para Question
- `QuestionnaireRepositoryMongo`: implementa in/out ports para Questionnaire

## Configuração

Em `application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/orderquestionnaire
      database: orderquestionnaire
```

Veja [`docker/mongo/init.js`](../../../../docker/mongo/init.js) para seeding inicial.

## Testes

- Testcontainers + embedded Mongo para testes de integração

