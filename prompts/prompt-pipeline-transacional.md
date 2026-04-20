# Prompt: Smart Pipeline com Rollback Transacional (Banco de Dados)

**Objetivo:** Implementar uma esteira de execução ("Smart Pipeline") em Java que respeite a Arquitetura Hexagonal e utilize o gerenciamento de transações nativo do banco de dados para reversão de falhas.

---

### Requisitos Técnicos:

1. **Purismo Hexagonal:**
    - A lógica de orquestração da esteira deve residir no Domínio, mantendo-se agnóstica a tecnologias de persistência ou frameworks de DI.

2. **Result Pattern:**
    - Os Steps devem retornar um objeto `Result<T>`.
    - O Use Case deve avaliar o resultado e, em caso de falha, sinalizar a necessidade de Rollback para a camada de Infraestrutura.

3. **Configuração e Ordem:**
    - Na camada de **Infraestrutura**, implemente a lógica que lê a sequência das etapas de um arquivo de configuração.
    - Use um `Comparator` para ordenar os Beans dos Steps antes de injetá-los no Use Case. Deve ser possível desativar etapas via configuração sem alterar o código.

4. **Data Bag Context com Type-Safety:**
    - Implemente o objeto de contexto utilizando a abordagem de Mapa de Tipos (`Map<Class<?>, Object>`).
    - Garanta que cada etapa possa ler e escrever dados de forma tipada e segura.

5. **Rollback Transacional:**
    - O rollback deve ser delegado ao Banco de Dados (ex: `@Transactional` do Spring).
    - Demonstre como o adaptador de entrada (Controller ou Command Handler) ou a classe de Configuração deve tratar o `Result.fail()` do Use Case para disparar o `rollback` da transação (ex: lançando uma exceção de infraestrutura capturada por um Interceptor).

### O que deve ser gerado:
- Classes de Domínio: `Step`, `Result`, `UseCase` e `PipelineContext`.
- O motor da esteira dentro do `UseCase`.
- A classe de Configuração na Infraestrutura responsável por montar a esteira ordenada.
- Exemplo de como a transação é aberta e fechada na borda da aplicação (Infra), reagindo ao `Result` do Domínio.
