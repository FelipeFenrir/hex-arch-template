# Loki

Backend de logs para consulta no Grafana.

## Arquivos desta pasta

- `config.yml`: servidor, schema, limites de ingestao e armazenamento local.

## Configuracao atual

- `auth_enabled: false` (uso local)
- Porta HTTP: `3100`
- Schema TSDB `v13`
- Armazenamento filesystem em `/loki/*`

## Uso no compose

Servico: `loki`

- Porta host: `3100`
- Config montada em: `/etc/loki/config.yml`

`promtail` envia logs para `http://loki:3100/loki/api/v1/push`.

