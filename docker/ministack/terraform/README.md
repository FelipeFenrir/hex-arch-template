# MiniStack Terraform

Terraform stack for local MiniStack resources. This folder is the single source of truth for AWS local resources used by the project.

## Managed resources

- S3 buckets: `my-test-bucket`, `test-bucket-1`, `test-bucket-2`, `logs-archive`
- SQS queues: `my-local-queue`, `task-queue`, `notifications`, `dlq-messages`
- SNS topics: `order-events`, `alerts`, `notifications`
- SNS -> SQS binding: topic `notifications` subscribed to queue `notifications` with queue policy allowing `sqs:SendMessage`

## State policy

- Backend: local (`terraform.tfstate`)
- Workspace policy: use a single workspace named `local`
- State and Terraform cache files are ignored by `.gitignore`

## Usage

1. Start MiniStack first (`docker compose -f docker/docker-compose.yml up -d ministack`).
2. Apply Terraform:

```powershell
Set-Location "D:\Projetos\hex-arch-template\docker\ministack\terraform"
.\apply-local.ps1
```

## Manual commands

```powershell
Set-Location "D:\Projetos\hex-arch-template\docker\ministack\terraform"
terraform init
terraform workspace select local
terraform plan
terraform apply
```

If `local` does not exist yet, run `terraform workspace new local` once.

