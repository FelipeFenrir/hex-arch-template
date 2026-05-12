# AWS Manager

Interface web para gerenciar recursos AWS no **MiniStack** (LocalStack).

Combina leitura genérica de serviços (inspirada no StackPort) com CRUD real de S3, SQS e SNS.

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
│   ├── create_test_resources.py   # Cria buckets/queues/topics de exemplo no MiniStack
│   └── verify_resources.py        # Confirma que os recursos estão acessíveis
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
# Criar recursos de teste no MiniStack
py -3 scripts/create_test_resources.py

# Verificar se recursos aparecem via backend
py -3 scripts/verify_resources.py
```

---

## Funcionalidades da SPA

| Feature                | Descrição                                                      |
|------------------------|----------------------------------------------------------------|
| Dashboard              | Cards por serviço com contadores, status e favoritos           |
| Resource Browser       | Listagem genérica (todos os 35+ serviços do SERVICE_REGISTRY)  |
| Detail Drawer          | Clique em um recurso para abrir drawer lateral com JSON        |
| Export JSON / CSV      | Botão por tipo de recurso; download automático                 |
| Atalhos de teclado     | `/` buscar · `j`/`k` navegar · `Enter` detalhar · `Esc` fechar |
| S3 CRUD                | Criar, atualizar versioning, deletar buckets                   |
| SQS CRUD               | Criar filas, enviar mensagens, assinar SNS                     |
| SNS CRUD               | Criar tópicos, publicar mensagens                              |
| MiniStack Health Chip  | Status em tempo real no topbar                                 |
| Favoritos + Grid/List  | Persistidos em `localStorage`                                  |
