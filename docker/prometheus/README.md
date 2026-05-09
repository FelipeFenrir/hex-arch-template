# Prometheus

Coleta e armazena metricas do ambiente local.

## Arquivos desta pasta

- `prometheus.yml`: jobs e targets de scrape.

## Targets configurados

- `prometheus:9090` (auto monitoramento)
- `host.docker.internal:9001/actuator/prometheus` (security-server no host)
- `security-server:9001/actuator/prometheus` (security-server em container opcional)
- `otel-collector:9464`
- `loki:3100`
- `tempo:3200`

Intervalos globais configurados: `5s`.

## Uso no compose

Servico: `prometheus`

- Porta host: `9090`
- Config montada em: `/etc/prometheus/prometheus.yml`

