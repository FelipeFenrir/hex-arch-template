# Cloud-AWS Adapter (Outbound) - OrderQuestionnaire

Publicação de eventos para SQS/SNS. Estado: **scaffold** (sem `build.gradle` ativo).

## Quando Ativar

Quando precisar publicar eventos de domínio (ex.: `QuestionnaireCreatedEvent`, `QuestionnaireAnswersValidatedEvent`) para processamento assíncronos por sistemas downstream.

## O Que Fazer

1. Criar `build.gradle` com AWS SDK v2 (SQS, SNS)
2. Implementar `EventPublisherOutPort` via SQS/SNS
3. Mapear eventos de domínio para mensagens serializadas (JSON)
4. Configurar tópicos/filas em `application.yml` ou via AWS CloudFormation
5. Registrar como bean no bootstrap

## Testes

- Localstack/MiniStack para testes de integração (veja [`docker/docker-compose.yml`](../../../../docker/docker-compose.yml))

