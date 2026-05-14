# OpenTelemetry Collector

Ponto central de entrada de telemetria (OTLP) no ambiente local.

## Arquivos desta pasta

- `config.yml`: define receivers, processors e exporters.

## Pipeline configurado

- Receiver OTLP via gRPC e HTTP
- Processors:
  - `memory_limiter` (protege o collector contra pressao de memoria)
  - `resource/local` (enriquece telemetria com metadados comuns)
  - `batch` (agrupar envios para melhor eficiencia)
- Exporters:
  - traces -> `tempo` (OTLP HTTP)
  - metrics -> endpoint Prometheus interno (`:9464`)

## Metadados adicionados pelo collector

- `deployment.environment.name=local` (upsert)
- `service.namespace=acme` (insert se ausente no sinal original)

## Uso no compose

Servico: `otel-collector`

- Porta host OTLP gRPC: `4317`
- Porta host OTLP HTTP: `4318`
- Config montada em: `/etc/otel-collector-config.yml`

## Integracao com o resto do ambiente

- `prometheus` faz scrape de `otel-collector:9464`
- traces seguem para `tempo`

