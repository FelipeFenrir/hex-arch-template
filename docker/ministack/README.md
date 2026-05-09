# MiniStack

Emulacao local de servicos AWS para desenvolvimento.

## Arquivos desta pasta

- `init-resources.sh`: script executado no startup do container MiniStack.
- `.gitkeep`: placeholder para manter a pasta versionada.

## O que o script inicializa

- Cria bucket S3: `s3://my-test-bucket`
- Cria fila SQS: `my-local-queue`

## Uso no compose

Servico: `ministack`

- Porta host: `4566`
- Healthcheck: `http://localhost:4566/_ministack/health`
- Script montado em: `/etc/ministack/init/ready.d/init-resources.sh`

## Observacao

O compose atual tambem referencia MiniStack para outros servicos internos, mas esta documentacao foca somente o ambiente base local.

