# Promtail

Agente de coleta de logs para envio ao Loki.

## Arquivos desta pasta

- `config.yml`: scrape configs para Docker e logs em arquivo do host.

## Fontes de log configuradas

- Containers Docker via `docker_sd_configs` (`/var/run/docker.sock`)
- Logs do `security-server` rodando no host
- Logs do `orderquestionnaire` rodando no host

## Pipeline normalizado

O pipeline atual faz parse do JSON de log para:

- reaproveitar o campo `timestamp` da aplicacao como timestamp do Loki
- promover apenas `level` como label
- manter `correlationId` e `flowId` fora de labels para evitar alta cardinalidade

Os filtros por `correlationId` e `flowId` sao aplicados no Grafana via query, nao por indexacao no Loki.

## Volumes montados no compose

- `/var/lib/docker/containers` (read-only)
- `/var/run/docker.sock`
- `../security-server/modules/bootstrap/build/logs`
- `../orderquestionnaire/modules/bootstrap/build/logs`

## Uso no compose

Servico: `promtail`

- Dependencia: `loki`
- Porta HTTP interna: `9080` (nao exposta para host)

