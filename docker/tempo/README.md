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

## Uso no compose

Servico: `tempo`

- Porta host HTTP: `3200`
- Porta gRPC interna: `9095`
- Config montada em: `/etc/tempo/config.yml`

