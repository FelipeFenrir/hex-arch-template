# Tempo

Backend de traces distribuidos no ambiente local.

## Arquivos desta pasta

- `config.yml`: configuracao de receiver OTLP, ingestao e armazenamento local.

## Configuracao atual

- Recebe OTLP gRPC e HTTP
- Retencao de blocos: `1h`
- Armazenamento local em:
  - `/var/tempo/wal`
  - `/var/tempo/blocks`
- `metrics_generator` habilitado para:
  - `service-graphs`
  - `span-metrics`
- Remote write para `prometheus`

## Uso no compose

Servico: `tempo`

- Porta host HTTP: `3200`
- Porta gRPC interna: `9095`
- Config montada em: `/etc/tempo/config.yml`

## Integracao com Grafana Node Graph

O dashboard `trace-topology.json` usa o datasource `Tempo` para exibir um `Node Graph` dinamico.

Para isso funcionar, o `tempo` gera metricas de service graph e envia para o `prometheus`, que e referenciado pelo `serviceMap` do datasource `Tempo` no Grafana.

