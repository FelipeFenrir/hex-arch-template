# Grafana

Camada de visualizacao para metricas, logs e traces.

## Estrutura da pasta

- `dashboards/`: dashboards versionados em JSON
  - `app-observability.json`
  - `logs-json-loki.json`
- `provisioning/datasources/datasources.yml`: datasources pre-configuradas
- `provisioning/dashboards/dashboards.yml`: provider para carregar dashboards locais

## Datasources provisionadas

- Prometheus (`http://prometheus:9090`)
- Loki (`http://loki:3100`)
- Tempo (`http://tempo:3200`)

## Uso no compose

Servico: `grafana`

- Porta host: `3000`
- Autenticacao anonima habilitada (`Admin` em ambiente local)
- Dependencias: `loki`, `tempo`, `prometheus`

## Acesso rapido

- URL: `http://localhost:3000`

