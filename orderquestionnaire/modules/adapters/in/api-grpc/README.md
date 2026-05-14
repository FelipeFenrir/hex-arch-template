# API-gRPC Adapter - OrderQuestionnaire

Transporte gRPC para casos de uso. Estado: **scaffold** (sem `build.gradle` ativo).

## Quando Ativar

Quando precisar suportar clientes gRPC (ex.: backend-to-backend, mobile com Envoy).

## O Que Fazer

1. Criar `build.gradle` com dependências gRPC
2. Definir `.proto` files para Question e Questionnaire services
3. Gerar stubs via protoc
4. Implementar `QuestionnaireGrpcService` (tridutor proto ↔ command/query)
5. Registrar no bootstrap

Referência: [`modules/adapters/in/api-rest/`](../api-rest/) fornece padrão de equivalência.

