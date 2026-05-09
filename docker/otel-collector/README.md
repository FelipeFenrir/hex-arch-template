# OpenTelemetry Collector

Ponto central de entrada de telemetria (OTLP) no ambiente local.

## Arquivos desta pasta

- `config.yml`: define receivers, processors e exporters.

## Pipeline configurado

- Receiver OTLP via gRPC e HTTP
- Processor `batch`
- Exporters:
  - traces -> `tempo` (OTLP HTTP)
  - metrics -> endpoint Prometheus interno (`:9464`)

## Uso no compose

Servico: `otel-collector`

- Porta host OTLP gRPC: `4317`
- Porta host OTLP HTTP: `4318`
- Config montada em: `/etc/otel-collector-config.yml`

## Integracao com o resto do ambiente

- `prometheus` faz scrape de `otel-collector:9464`
- traces seguem para `tempo`

