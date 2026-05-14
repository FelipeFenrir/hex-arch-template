# Prompt Mestre - Analise, Refatoracao e Expansao de Testes com Piramide de Testes

Use este prompt para orientar a análise e a refatoracao completa dos testes existentes do projeto `orderquestionnaire`, com criacao de novos testes para fechar lacunas de cobertura e reduzir risco de regressao.

---

## Prompt para reutilizar

Voce vai atuar como especialista em qualidade de software para projetos Java com Spring e arquitetura hexagonal.

### Objetivo

Aplicar e consolidar a estrategia de **piramide de testes** no contexto do projeto `orderquestionnaire`, cobrindo:

1. Testes de arquitetura (ArchUnit)
2. Testes unitarios (JUnit 5 + Mockito, sem contexto Spring)
3. Testes de integracao (Spring parcial + JUnit 5 + Mockito + Testcontainers)
4. Testes de entrypoint (adaptadores de entrada REST/gRPC/fila/eventos)
5. Testes end-to-end (fluxo completo no bootstrap)

A entrega deve incluir:
- Diagnostico dos testes atuais
- Plano de refatoracao por modulo
- Refatoracao efetiva dos testes existentes
- Criacao de testes novos para completude de cobertura
- Evidencias de execucao (comandos + resultado resumido)

### Regras obrigatorias

- Respeitar a arquitetura hexagonal do repositorio.
- Priorizar modulos ativos do build (`orderquestionnaire/settings.gradle`).
- Nao introduzir dependencia de Spring em testes unitarios.
- Usar **JUnit 5** e **Mockito** como base.
- Para testes de acordo com a convencao solicitada:
  - Unitarios: usar `@UnitTest` e `@DisplayName`.
  - Integracao: usar `@IntegrationTest` e `@DisplayName`.
  - E2E: usar `@E2ETest` e `@DisplayName`.
  - Usar BDD com Cucumber em pacote especifico de BDD para cada tipo de teste quando aplicavel.
- Nao remover testes existentes sem justificar tecnicamente (duplicidade, fragilidade, baixa signal/noise).
- Sempre cobrir fluxo feliz, validacoes, erros de dominio e regressao de comportamento.
- Manter nomenclatura e pacote de testes coerentes com o modulo alvo.

### Estrutura-alvo da piramide (organizar pacotes de teste)

Padronize pacotes por tipo de teste dentro de cada modulo:

- `...architecture...` -> ArchUnit
- `...unit...` -> Unitarios
- `...integration...` -> Integracao
- `...entrypoint...` -> Entrypoint (somente modulos de entrada `adapters/in`)
- `...e2e...` -> E2E (somente modulo `bootstrap`)
- `...bdd...` -> Features, steps e runners Cucumber segregados por tipo

Se existir divergencia de pacote atual, mover gradualmente sem quebrar build, atualizando imports/utilitarios conforme necessario.

### Escopo minimo por tipo de teste

#### 1) Arquitetura (ArchUnit)
- Validar fronteiras hexagonais (domain nao depende de spring/adapters).
- Validar direcao de dependencias entre `domain`, `application`, `adapters` e `bootstrap`.
- Validar anotacoes de estereotipos relevantes quando houver regra no projeto.
- Cobrir obrigatoriamente `domain`, `application`, `adapters` e `bootstrap`.

#### 2) Unitarios
- Nao subir contexto Spring.
- Cobrir metodos unitarios com foco em regras de negocio.
- Validar `Result.Success` e `Result.Failure` (quando aplicavel no dominio/aplicacao).
- Cobrir casos positivos, negativos, borda e erros de dominio.
- Usar `@UnitTest` e `@DisplayName`.

#### 3) Integracao
- Subir apenas o necessario do Spring para integrar componentes reais.
- Usar Testcontainers quando houver dependencia externa (ex.: Mongo).
- Cobrir integracao entre modulo e dependencias reais (repositorios, mappers, adapters out).
- Usar `@IntegrationTest` e `@DisplayName` conforme convencao solicitada.

#### 4) Entrypoint
- Foco em adaptadores de entrada: REST, gRPC, SQS/SNS/Kafka/eventos.
- Validar contrato de entrada, headers, tenant/contexto e serializacao/deserializacao.
- Validar mapeamento de erro para resposta/protocolo correto.
- Usar `@UnitTest` e `@DisplayName` conforme convencao solicitada.

#### 5) E2E
- Criar no modulo `bootstrap`.
- Cobrir apenas fluxos criticos de ponta a ponta.
- Garantir observabilidade basica de falhas (asserts claros e diagnosticos uteis).
- Usar `@E2ETest` e `@DisplayName`.
- Usar BDD com Cucumber (features + suite + step defs) tambem para E2E.

### Modo de execucao (passo a passo obrigatorio)

1. **Inventariar** testes atuais por modulo e por tipo real (nao pelo nome do arquivo).
2. **Classificar** cada teste na piramide e apontar desalinhamentos de pacote/anotacao/escopo.
3. **Mapear lacunas**:
   - Regras sem teste
   - Erros de dominio sem teste
   - Entrypoints sem cobertura minima
   - Fluxos criticos sem E2E
4. **Propor plano incremental** (ordem de refatoracao), priorizando risco e impacto.
5. **Refatorar testes existentes** para o novo padrao de pacotes e estilo.
6. **Criar novos testes** para completude de cobertura no escopo definido.
7. **Executar testes** e reportar resultado por modulo/tipo.
8. **Entregar relatorio final** com diff logico do que mudou.

### Formato de saida esperado

Responder sempre nesta estrutura:

1. **Checklist de execucao**
   - [ ] Inventario
   - [ ] Classificacao
   - [ ] Lacunas
   - [ ] Plano
   - [ ] Refatoracao
   - [ ] Novos testes
   - [ ] Execucao
   - [ ] Relatorio final

2. **Diagnostico inicial**
   - Tabela: modulo | tipo atual | tipo alvo | acao necessaria | prioridade

3. **Plano de refatoracao**
   - Etapas numeradas por modulo
   - Risco, dependencia e criterio de pronto por etapa

4. **Implementacao aplicada**
   - Arquivos alterados/criados
   - Justificativa tecnica curta por mudanca

5. **Cobertura de cenarios**
   - Fluxo feliz
   - Falhas esperadas
   - Casos de borda
   - Regressao

6. **Evidencias de execucao**
   - Comandos executados
   - Resultado resumido (passou/falhou + principais erros)

7. **Pendencias e proximos passos**
   - O que ficou para iteracao seguinte
   - Riscos residuais

### Criterios de aceite

- Todos os testes estao classificados na piramide e organizados em pacotes coerentes.
- Testes unitarios sem contexto Spring.
- Testes de integracao/entrypoint/e2e com contexto e infraestrutura minima necessaria.
- BDD organizado em pacote especifico e com cenarios Given/When/Then.
- Cobertura funcional ampliada no escopo alterado (incluindo falhas e bordas).
- Sem quebra de arquitetura hexagonal.
- Execucao dos testes relevantes do modulo com resultado reportado.

### Comandos base (Windows PowerShell)

Ajuste conforme modulo/escopo da iteracao. Priorize execucao focada para evitar ruido de modulos fora do escopo.

```powershell
.\gradlew.bat :orderquestionnaire:modules:domain:test
.\gradlew.bat :orderquestionnaire:modules:application:test
.\gradlew.bat :orderquestionnaire:modules:adapters:out:mongo:test
.\gradlew.bat :orderquestionnaire:modules:adapters:out:api-distribution:test
.\gradlew.bat :orderquestionnaire:modules:bootstrap:test
.\gradlew.bat :orderquestionnaire:modules:domain:pitest
.\gradlew.bat :orderquestionnaire:modules:application:pitest
.\gradlew.bat :orderquestionnaire:modules:adapters:out:mongo:pitest
.\gradlew.bat :orderquestionnaire:modules:adapters:out:api-distribution:pitest
.\gradlew.bat :orderquestionnaire:modules:bootstrap:pitest
```

### Observacoes importantes do repositorio

- O repositorio tem modulos scaffold/nao ativos no Gradle dentro de `orderquestionnaire/modules/adapters/in` e `orderquestionnaire/modules/adapters/out/cloud-aws`; confirmar inclusao no `settings.gradle` antes de acoplar pipeline de testes.
- Existe historico de falha em `clean test` na raiz por dependencia circular em outro contexto; quando necessario, executar tarefas focadas do composite `:orderquestionnaire:`.

---

## Como usar este arquivo

Copie o conteudo da secao **Prompt para reutilizar** e cole na conversa com o agente, adicionando no topo:

- Modulo(s) alvo(s)
- Escopo funcional da mudanca
- Meta de cobertura para a iteracao
- Limite de tempo/prioridade (ex.: primeiro domain, depois adapters)

