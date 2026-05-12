# Queue-SQS Adapter (Inbound) - OrderQuestionnaire

Consumo de eventos SQS para cases de uso assíncronos. Estado: **scaffold** (sem `build.gradle` ativo).

## Quando Ativar

Quando precisar processar questionários ou respostas via filas (ex.: batch validation, relatórios assíncronos).

## O Que Fazer

1. Criar `build.gradle` com Spring Cloud AWS, AWS SDK v2
2. Configurar listeners SQS via `@SqsListener` ou `SimpleMessageListenerContainer`
3. Implementar deserialização de eventos
4. Mapear eventos para commands (ex.: `QuestionnaireAnswerSubmittedEvent` → `ValidateQuestionnaireAnswersCommand`)
5. Invocar use cases
6. Publicar resultados via outbound adapter (SNS/SQS)

Referência: [`modules/adapters/in/api-rest/`](../api-rest/) fornece padrão de entrada/tradução.

