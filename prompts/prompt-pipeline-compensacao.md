# Prompt: Smart Pipeline com Rollback por Compensação (Saga Pattern)

**Objetivo:** Criar um motor de execução de Casos de Uso (Use Case) em Java seguindo a Arquitetura Hexagonal, onde a lógica de negócio é decomposta em uma esteira (Pipeline) de etapas (Steps) independentes e configuráveis.

---

### Requisitos Técnicos Mandatórios:

1. **Purismo Hexagonal e Injeção:**
    - O núcleo (Domínio) deve ser agnóstico a frameworks. O `UseCase` recebe uma `List<Step>` ordenada via construtor.
    - A montagem da lista deve ocorrer na camada de **Infraestrutura**, utilizando um `Comparator` que lê a ordem de um arquivo de configuração (YAML/JSON) e filtra steps desativados.

2. **Result Pattern com Short-Circuit:**
    - Cada `Step` deve retornar obrigatoriamente um objeto `Result<T>`.
    - O `UseCase` deve implementar um "Short-Circuit": ao encontrar o primeiro `Result.fail()`, a execução da esteira para e inicia o rollback. Exceções técnicas dentro dos Steps devem ser capturadas e convertidas em `Result.fail()`.

3. **Data Bag Context com Type-Safety:**
    - O contexto de execução deve ser um mapa tipado: `Map<Class<?>, Object>`.
    - Implemente métodos genéricos `<T> void put(T value)` e `<T> T get(Class<T> type)` usando `type.cast()` para evitar *unchecked casts* e chaves baseadas em String.

4. **Rollback por Compensação (Pilha LIFO):**
    - O `UseCase` deve gerenciar uma pilha (`java.util.Deque`) contendo apenas as etapas que retornaram `Result.ok()`.
    - Em caso de falha, o orquestrador deve percorrer essa pilha desempilhando e executando o método `rollback(context)` de cada etapa concluída.

5. **Encapsulamento:**
    - Steps não devem conhecer uns aos outros nem o UseCase; a única comunicação permitida é através da leitura/escrita no Objeto de Contexto.

### O que deve ser gerado:
- Interfaces de Domínio: `Step<T>`, `Result<T>`, `UseCase<T>`.
- Implementação da classe `PipelineContext` (Data Bag).
- Implementação do `UseCase` orquestrador com lógica de execução e rollback.
- Exemplo de configuração (Spring @Configuration ou similar) demonstrando o `Comparator` ordenando os steps por um ID externo.
