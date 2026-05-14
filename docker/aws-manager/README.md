# AWS Manager

Interface web para gerenciar recursos AWS no **MiniStack** (LocalStack).

Combina leitura genérica de serviços (inspirada no StackPort) com CRUD real de S3, SQS, SNS e DynamoDB.

---

## Stack

| Camada     | Tecnologia                                      |
|------------|-------------------------------------------------|
| Frontend   | React 18 + Vite + TypeScript                    |
| Backend    | Python 3.11 + Flask + boto3                     |
| Infra local| MiniStack (porta `4566`)                        |

---

## Estrutura

```
aws-manager/
├── aws_manager/           # Pacote Flask (backend)
│   ├── __init__.py        # create_app() — registra blueprints + serve SPA
│   ├── cache.py           # TTLCache em memória
│   ├── clients.py         # _build_client() + _resolve_endpoint() com auto-detect
│   ├── config.py          # _resolve_active_endpoint() (proba ministack/localhost)
│   ├── registry.py        # SERVICE_REGISTRY, DESCRIBE_REGISTRY, ID_FIELDS
│   ├── utils.py           # Helpers (parse_json_field, normalize_sqs_endpoint)
│   ├── web/
│   │   └── api.py         # Blueprint: /api/health, /api/stats, /api/resources/*, /api/ministack/health
│   ├── s3/
│   │   ├── clients.py     # build_s3_client()
│   │   ├── services.py    # create_bucket, delete_bucket, update_bucket_settings, …
│   │   └── web/api.py     # Blueprint: /api/s3/buckets (CRUD)
│   ├── sqs/
│   │   ├── clients.py     # build_sqs_client()
│   │   ├── services.py    # list_available_queues, send_message, …
│   │   └── web/api.py     # Blueprint: /api/sqs/queues (CRUD)
│   ├── dynamodb/
│   │   ├── clients.py     # build_dynamodb_client(), build_dynamodb_resource()
│   │   ├── services.py    # create_table, update_table_settings, table items CRUD
│   │   └── web/api.py     # Blueprint: /api/dynamodb/tables (CRUD + items)
│   └── sns/
│       ├── clients.py     # build_sns_client()
│       ├── services.py    # list_available_topics, publish, subscribe, …
│       └── web/api.py     # Blueprint: /api/sns/topics (CRUD)
├── ui/                    # Frontend React/Vite
│   ├── src/
│   │   ├── App.tsx        # Rotas, Dashboard, ResourceBrowser, S3/SQS/SNS views
│   │   ├── api.ts         # Fetch helpers e tipos
│   │   ├── toast.tsx      # Toast context (auto-dismiss 3.2s)
│   │   ├── styles.css
│   │   ├── components/
│   │   │   ├── Sheet.tsx       # Drawer lateral para detalhe de recurso
│   │   │   └── JsonViewer.tsx  # Árvore JSON colorida
│   │   ├── hooks/
│   │   │   └── useKeyboardShortcuts.ts  # /, j, k, Enter, Esc, r, [, ]
│   │   └── lib/
│   │       └── export.ts        # Export JSON/CSV com download automático
│   └── dist/              # Build Vite (gerado; servido pelo Flask)
├── scripts/               # Utilitários locais (não fazem parte do build/CI)
│   └── verify_resources.py        # Confirma que os recursos provisionados via Terraform estão acessíveis
├── smoke_test.py          # Suite de smoke tests (mocks, sem infra real)
├── requirements.txt
├── Dockerfile             # Multi-stage: Node (Vite build) → Python slim
└── README.md
```

---

## Executar localmente

### 1. Subir infra (MiniStack + dependências)

```bash
docker compose -f docker/docker-compose.yml up -d
```

### 2. Instalar dependências Python

```bash
cd docker/aws-manager
pip install -r requirements.txt
```

### 3. Build do frontend

```bash
cd ui
npm ci
npm run build
```

### 4. Iniciar Flask

```bash
cd docker/aws-manager
python -m flask --app aws_manager run --host 0.0.0.0 --port 5000
```

Acesse **http://localhost:5000**.

> **Auto-detecção de endpoint:** o backend proba `ministack:4566` → `localhost:4566` → `127.0.0.1:4566`
> automaticamente. Nenhuma variável de ambiente é necessária para uso local.

---

## Smoke tests

```bash
cd docker/aws-manager
py -3 smoke_test.py
# smoke-tests-ok
```

Usam mocks; não necessitam MiniStack rodando.

---

## Scripts de desenvolvimento

```bash
# Verificar se recursos aparecem via backend
py -3 scripts/verify_resources.py
```

> A criacao de recursos no MiniStack e feita via Terraform em `docker/ministack/terraform`.

---

## Funcionalidades da SPA

| Feature                | Descrição                                                      |
|------------------------|----------------------------------------------------------------|
| Dashboard              | Cards por serviço com contadores, status e favoritos           |
| Resource Browser       | Listagem genérica (todos os 35+ serviços do SERVICE_REGISTRY)  |
| Detail Drawer          | Clique em um recurso para abrir drawer lateral com JSON        |
| Sidebar retratil       | Menu lateral pode ser recolhido para ampliar area de conteudo  |
| Export JSON / CSV      | Botão por tipo de recurso; download automático                 |
| Atalhos de teclado     | `/` buscar · `j`/`k` navegar · `Enter` detalhar · `Esc` fechar |
| S3 CRUD                | Criar, atualizar versioning, deletar buckets                   |
| SQS CRUD               | Criar filas, editar atributos, enviar mensagens e deletar com confirmacao de seguranca |
| SNS CRUD               | Criar topicos, editar display name, publicar e deletar com confirmacao de seguranca |
| DynamoDB CRUD          | Criar/editar/deletar tabelas e gerenciar itens por JSON        |
| MiniStack Health Chip  | Status em tempo real no topbar                                 |
| Favoritos + Grid/List  | Persistidos em `localStorage`                                  |
