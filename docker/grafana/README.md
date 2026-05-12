# Grafana

Camada de visualizacao para metricas, logs e traces.

## Estrutura da pasta

- `dashboards/`: dashboards versionados em JSON
  - `app-observability.json`
  - `logs-json-loki.json`
  - `trace-topology.json`
  - `trace-investigation.json`
- `provisioning/datasources/datasources.yml`: datasources pre-configuradas
- `provisioning/dashboards/dashboards.yml`: provider para carregar dashboards locais

## Datasources provisionadas

- Prometheus (`http://prometheus:9090`)
- Loki (`http://loki:3100`)
- Tempo (`http://tempo:3200`)

## Correlacao logs <-> traces

- Loki possui `derivedFields` para detectar `traceId` no JSON de log e abrir o trace no Tempo.
- Tempo usa `tracesToLogsV2` com filtro por `traceId` e `spanId` para buscar logs correlacionados no Loki.
- A janela temporal da busca trace->logs usa `-10m/+10m` para reduzir perda de eventos em cenarios com latencia/retries.

## Dashboard de logs

O dashboard `logs-json-loki.json` foi configurado para:

- mostrar o conteudo de `message` como payload legivel
- usar o time picker do Grafana para filtro por data e hora
- permitir filtros por `App`, `Mode`, `Level`, `Correlation ID`, `Flow ID`, `Trace ID` e `Span ID`

`Correlation ID` e `Flow ID` usam filtro textual/regex na query do Loki e nao sao labels indexadas.

## Dashboard de Observabilidade de Aplicacao (App Observability)

O dashboard `app-observability.json` eh uma visao operacional completa com os **4 Golden Signals** + Availability/SLO + Drill-down por endpoint.

### Paginas e Secos

#### 1. **Overview - KPIs & Golden Signals** (topo)
Cards stat com metricas agregadas:
- **Business Targets Up**: contagem de apps de negocio ativas
- **Total Throughput**: requisicoes/segundo globais
- **Error Rate (5xx)**: taxa percentual de erros servidor
- **p95 Latency (worst)**: pior p95 entre todas as apps

#### 2. **Availability & SLO Compliance**
- **Availability (uptime)**: percentual de tempo que pelo menos uma app estao disponevel em janela de 1h
- **SLO Compliance (error rate)**: mostra se a taxa de erro atual esta abaixo ou acima do limiar (padrão 1.0%)
- **Error Budget (remaining)**: percentual do budget de erro SLO restante nesta janela (útil para planejamento de deploys)
- **SLO Status by App**: tabela colorida mostrando erro rate de cada aplicacao vs SLO threshold

#### 3. **Golden Signals - Detailed View**
- **Traffic (requests/sec by app)**: série temporal de throughput por aplicacao
- **Errors (5xx rate % by app)**: série temporal de taxa de erro por aplicacao com limites visuais
- **Latency (p95 & p99 by app)**: p95 e p99 agregados por app em uma unica serie, facilitando comparacao

#### 4. **Saturation (Resource Utilization)**
- **Heap Usage (JVM %)**: utilizacao de heap por app (amarelo > 70%, vermelho > 85%)
- **CPU Usage (%)**: uso de CPU por app (amarelo > 60%, vermelho > 80%)
- **Live Threads (count)**: numero de threads vivas
- **HTTP/Pool Utilization (%)**: ocupacao de thread pool (Tomcat + Executor)

#### 5. **Detailed Analysis - Endpoints & Status Breakdown** (drill-down)
- **Endpoints by Status**: top 10 endpoints ordenados por throughput, com coloracao por status code (2xx/3xx verde, 4xx amarelo, 5xx vermelho)
- **Error Endpoints (5xx)**: top 15 endpoints que estao gerando erros 5xx
- **Responses by Status Code**: serie temporal separada por ranges (2xx, 3xx, 4xx, 5xx) para ver tendencia de qualidade

#### 6. **Platform Infrastructure Health**
- **Platform Components Up**: disponibilidade dos servicos de observabilidade (prometheus, otel-collector, loki, tempo)
- **Platform Scrape Duration**: latencia de coleta de metricas de cada componente

### Filtros Disponíveis

- **Business App**: dropdown/multi-select com apps de negócio (`security-server`, `orderquestionnaire`) extraídos de jobs `-host` e `-container`. Afeta todos os painéis de golden signals, SLO e drill-down.
- **Platform Component**: dropdown/multi-select com componentes de infraestrutura (`prometheus`, `otel-collector`, `loki`, `tempo`). Afeta apenas painéis de plataforma.
- **Rate Window**: `1m`, `5m` ou `15m` — janela de tempo para cálculo de rates e histogramas. Padrão: `5m`.
- **SLO Error Threshold (%)**: `0.1`, `0.5`, `1.0`, `5.0` — limiar de taxa de erro aceitável para SLO. Padrão: `1.0%`. Usado nas metricas de compliance.
- **HTTP Method** (novo): `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `HEAD`, `OPTIONS` — filtra drill-down por método HTTP. Padrão: **All**. Útil para isolar problemas em endpoints específicos (ex: apenas `POST` está falhando).

### Links Automáticos para Rastreamento

O dashboard agora inclui **5 painéis com links diretos** para outros dashboards:

- **"SLO Status by App"**: clique na abreviação de uma app para abrir Trace Topology filtrado naquela app com erros (ERROR level).
- **"Errors (5xx rate % by app)"**: clique na série temporal de erro para pular para Trace Topology.
- **"Error Endpoints (5xx) - with Trace Link"** (novo painel): cada linha da tabela tem 2 links:
  - 🔍 "Trace this error in Trace Topology"
  - 📊 "View logs in Loki (5xx errors)"
- **"Responses by Status Code"**: clique na série `5xx` (em vermelho, destacada com linha grossa) para ir a Trace Topology.
- **"Quick Navigation: Jump to Trace Topology"** (novo painel): 4 botões de atalho:
  - 📊 View ALL Error Traces
  - 🔍 View Traces for Selected App
  - 📋 View Logs in Loki
  - 🕐 View Request Timeline (Trace Investigation)

### Estratégia de Normalizacao de Labels

No curto prazo o dashboard usa o label `job` para garantir compatibilidade com targets Prometheus host/container:
- `security-server-host`, `security-server-container`
- `orderquestionnaire-host`, `orderquestionnaire-container`

Quando os resource attributes (`service.name`, `service.namespace`) estiverem mais consistentes nos exports de metrics via OpenTelemetry, a selecao pode evoluir para usar labels Prometheus derivados desses atributos.

### Interpretar os Painéis

**Traffic**: aumento repentino ou queda pode indicar:
- Surge de usuarios (pico) ou problema de roteamento/LB
- Deploy bem-sucedido (aumento de capacidade) ou falha (queda)

**Latency (p95/p99)**: valores crescentes indicam:
- Degradacao de performance (possível GC, I/O lento, recurso saturado)
- Problema downstream (BD, cache, API externa)

**Errors**: saltos na taxa de erro sugerem:
- Bugs na aplicacao (crash, validacao stricta, timeout)
- Problema de dependencia (BD indisponível, API retornando 5xx)
- SEMPRE investigar via drill-down "Error Endpoints (5xx)" ou "Correlated Flow Logs" no dashboard de traces

**Saturation (Heap/CPU/Threads)**: sinais de proximidade ao limite:
- Heap > 85%: GC cada vez mais frequente, risco de OOM
- CPU > 80%: possível gargalo de processamento, considere escalar horizontalmente
- Threads vivas > limite config: pool saturado, requisicoes enfileiradas, latencia aumenta

**Error Budget**: ferramenta for planejamento:
- Se budget > 50%: margem confortavel, safe para deploys/manutencao
- Se budget < 25%: be careful, erros estao consumindo budget rápido
- Se budget < 10%: STOP — investigue e corrija antes de liberar features novas

## Dashboards de tracing

- `trace-topology.json`: visao principal com `Node Graph` dinamico (service graph do Tempo) + mapa de referencia da arquitetura e filtros por `correlationId`, `traceId`, `spanId`.
- `trace-investigation.json`: visao de investigacao com cards de sucesso/warn/error e timeline de warnings/erros para o contexto filtrado.

Para o `Node Graph` funcionar com dados vivos, o `Tempo` precisa gerar metricas de service graph e enviar para o `Prometheus` via remote write.

Se o painel estiver vazio, normalmente significa que ainda nao houve trafego instrumentado suficiente para gerar series `service graph` no `Tempo`/`Prometheus`.

## Uso no compose

Servico: `grafana`

- Porta host: `3000`
- Autenticacao anonima habilitada (`Admin` em ambiente local)
- Dependencias: `loki`, `tempo`, `prometheus`

## Acesso rapido

- URL: `http://localhost:3000`
- **Novo:** Guias de uso direto em [`APP-OBSERVABILITY-GUIDE.md`](APP-OBSERVABILITY-GUIDE.md) e [`APP-OBSERVABILITY-CHEATSHEET.md`](APP-OBSERVABILITY-CHEATSHEET.md)

