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

