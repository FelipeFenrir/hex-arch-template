# Prompt: Smart Pipeline com Rollback por Compensação (Saga Pattern)

**Objetivo:** Implementar uma estrutura de "Smart Pipeline" em Java utilizando Arquitetura Hexagonal, onde o Caso de Uso é um orquestrador de etapas configuráveis.

---

### Requisitos Técnicos:

1. **Purismo Hexagonal:**
    - O Domínio (Use Case, Interfaces de Step e Contexto) deve ser 100% puro Java, sem anotações de frameworks (Spring/Jakarta).
    - O Use Case deve receber uma `List<Step>` pronta via construtor (Injeção de Dependência via Configuração).

2. **Result Pattern:**
    - Cada Step deve retornar um objeto `Result<T>` que encapsula sucesso ou falha.
    - O fluxo deve ser interrompido imediatamente em caso de falha (Short-circuit).

3. **Configuração Dinâmica e Ordem:**
    - Na camada de **Infraestrutura**, implemente uma Factory/Configuration que utilize um `Comparator`.
    - Este comparador deve ler a ordem de execução e o status (ativo/inativo) de cada Step a partir de uma fonte externa (YAML, JSON ou Mock de config).

4. **Data Bag Context com Type-Safety:**
    - Crie um objeto de contexto baseado em um mapa de tipos: `Map<Class<?>, Object>`.
    - Implemente métodos genéricos `<T> void put(T value)` e `<T> T get(Class<T> type)` para garantir segurança de tipos sem uso de chaves String ou casting manual no domínio.

5. **Rollback por Compensação (Saga):**
    - O orquestrador (Use Case) deve manter uma Pilha (`Deque/Stack`) das etapas concluídas com sucesso.
    - Se um Step retornar `Result.fail()`, o orquestrador deve percorrer essa pilha e executar o método `rollback(context)` de cada etapa anterior para desfazer ações (como chamadas de API ou limpezas).

### O que deve ser gerado:
- Interfaces de Domínio: `Step`, `Result`, `UseCase`.
- Implementação do `PipelineContext` (Data Bag).
- O código do `UseCase` orquestrador com a lógica de Pilha/Rollback.
- Exemplo de um `Step` concreto com lógica de execução e compensação.
- A classe de Configuração (Infra) que monta o Bean utilizando o `Comparator`.
