# Prompt: Smart Pipeline com Rollback Transacional (Database-Driven)

**Objetivo:** Implementar uma esteira de execução ("Smart Pipeline") em Java que utilize o gerenciamento de transações do Banco de Dados para garantir a atomicidade de um Caso de Uso decomposto em múltiplos Steps.

---

### Requisitos Técnicos Mandatórios:

1. **Purismo Hexagonal e Borda Transacional:**
    - O `UseCase` (Domínio) coordena a execução dos Steps de forma agnóstica, mas deve sinalizar falhas de forma que a camada de **Infraestrutura** possa reagir.
    - A transação deve ser aberta na Borda (Adaptador de Entrada ou Configuração de Infra) e não dentro do Domínio.

2. **Result Pattern e Sinalização de Rollback:**
    - Utilize o `Result Pattern` para retornos de cada etapa.
    - Como transações de banco de dados SQL dependem geralmente de Exceções para Rollback (ex: Spring `@Transactional`), o orquestrador deve converter um `Result.fail()` em uma `RuntimeException` específica (ex: `PipelineTransactionException`) apenas no ponto de saída da esteira para forçar o descarte da transação.

3. **Configuração via Comparator:**
    - A ordem dos Steps deve ser definida externamente (YAML/JSON).
    - Na camada de Infraestrutura, utilize um `Comparator` para injetar a `List<Step>` no UseCase na sequência correta. Steps marcados como `enabled: false` na config devem ser ignorados.

4. **Data Bag Context com Type-Safety:**
    - O tráfego de dados entre os Steps deve ocorrer via mapa de classes (`Map<Class<?>, Object>`).
    - Garanta que o acesso aos dados seja feito por `context.get(AlgumaClasse.class)`, garantindo que cada Step tenha apenas o que precisa.

5. **Robustez:**
    - Cada Step deve ser responsável por validar se os dados necessários para sua execução estão presentes no contexto antes de iniciar a lógica de banco de dados.

### O que deve ser gerado:
- Classes de Domínio: `Step`, `Result`, `UseCase` e `PipelineContext`.
- O motor da esteira no `UseCase` avaliando sucessos/falhas.
- Exemplo de um Adaptador de Entrada (ou Service de Infra) que inicia a `@Transactional`, chama o UseCase e trata a exceção gerada pelo `Result.fail()`.
- Lógica do `Comparator` para montagem dinâmica da lista de steps.
