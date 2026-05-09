# Promtail

Agente de coleta de logs para envio ao Loki.

## Arquivos desta pasta

- `config.yml`: scrape configs para Docker e logs em arquivo do host.

## Fontes de log configuradas

- Containers Docker via `docker_sd_configs` (`/var/run/docker.sock`)
- Logs do `security-server` rodando no host
- Logs do `orderquestionnaire` rodando no host

## Volumes montados no compose

- `/var/lib/docker/containers` (read-only)
- `/var/run/docker.sock`
- `../security-server/modules/bootstrap/build/logs`
- `../orderquestionnaire/modules/bootstrap/build/logs`

## Uso no compose

Servico: `promtail`

- Dependencia: `loki`
- Porta HTTP interna: `9080` (nao exposta para host)

