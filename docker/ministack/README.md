# MiniStack

Emulacao local de servicos AWS para desenvolvimento.

## Arquivos desta pasta

- `terraform/`: stack Terraform com os recursos AWS locais versionados.
- `.gitkeep`: placeholder para manter a pasta versionada.

## O que o Terraform inicializa

- Cria bucket S3: `s3://my-test-bucket`
- Cria fila SQS: `my-local-queue`
- Cria recursos adicionais usados em testes manuais do AWS Manager (`test-bucket-1`, `task-queue`, `order-events`, etc.)
- Cria binding SNS -> SQS (`notifications` topic para `notifications` queue) com policy de envio

## Uso no compose

Servico: `ministack`

- Porta host: `4566`
- Healthcheck: `http://localhost:4566/_ministack/health`
- Provisionamento de recursos: executado via Terraform em `docker/ministack/terraform`

## Fluxo recomendado

1. Subir MiniStack com Docker Compose.
2. Aplicar Terraform em `docker/ministack/terraform` (workspace unico `local`).

## Observacao

O compose atual tambem referencia MiniStack para outros servicos internos, mas esta documentacao foca somente o ambiente base local.

