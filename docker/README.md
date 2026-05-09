# Docker local environment

Este diretorio existe para simular um ambiente de producao na maquina do desenvolvedor.

> Importante: este ambiente **nao substitui** testes unitarios, testes de integracao, BDD, nem testes com Testcontainers. Ele apoia o desenvolvimento local e a validacao manual/observabilidade.

## Escopo desta documentacao

Esta documentacao cobre:

- `docker-compose.yml`
- subpastas de infraestrutura usadas por ele

Esta documentacao **nao cobre por enquanto**:

- `aws-manager/`
- `stackport/`

## O que sobe com `docker-compose.yml`

Os servicos estao divididos em tres categorias. E importante entender essa separacao para saber o que e infraestrutura de suporte e o que e uma aplicacao real desenvolvida pelo time.

---

### Infraestrutura base

Servicos fundamentais que a maioria das aplicacoes precisa para funcionar localmente.

| Servico | Funcao | Porta host |
|---|---|---:|
| `mongo` | Banco de dados MongoDB | `27017` |
| `mongo-express` | Interface web para inspecionar o MongoDB | `8081` |
| `ministack` | Emulacao local de servicos AWS (SQS, SNS, S3) | `4566` |
| `wiremock` | Mock de APIs externas consumidas pelas aplicacoes | `8082` |

---

### Stack de observabilidade

Servicos de telemetria que permitem visualizar logs, metricas e traces gerados pelas aplicacoes. Nao sao aplicacoes de negocio — sao ferramentas de monitoramento que replicam o comportamento do ambiente produtivo.

| Servico | Funcao | Porta host |
|---|---|---:|
| `otel-collector` | Ponto central de entrada de telemetria OTLP | `4317` (gRPC), `4318` (HTTP) |
| `prometheus` | Coleta e armazena metricas das aplicacoes | `9090` |
| `tempo` | Armazena traces distribuidos | `3200` |
| `loki` | Armazena e indexa logs | `3100` |
| `promtail` | Coleta logs dos containers e do host e envia para o Loki | - |
| `grafana` | Dashboard para visualizar metricas, logs e traces | `3000` |

> **Dica:** Na porta `9090` roda o **Prometheus** (ferramenta de metricas), nao uma aplicacao de negocio. Para ver dashboards visuais acesse o **Grafana** em `http://localhost:3000`.

---

### Aplicacoes (opcionais via profile)

Sao as **aplicacoes reais desenvolvidas pelo time** que podem subir em container para simular o ambiente produtivo completo. Por padrao elas **nao sobem** com o `docker compose up` simples — precisam ser habilitadas explicitamente.

> **Importante:** ao contrario dos servicos de infraestrutura acima, estas aplicacoes contem logica de negocio propria do projeto. Se voce estiver desenvolvendo localmente via IDE ou Gradle, **nao e necessario** subi-las aqui.

| Servico | Funcao | Porta host | Como ativar |
|---|---|---:|---|
| `app` (`security-server`) | Servidor de autenticacao e autorizacao (OAuth2, login, tokens JWT) | `9001` | `--profile app-container` |

**Quando usar o `security-server` em container:**
- Para validar o fluxo completo de autenticacao sem rodar a aplicacao na IDE.
- Para testes manuais de ponta a ponta com OAuth2/JWT.

**Quando NAO usar:**
- Durante o desenvolvimento ativo da propria aplicacao — rode-a pela IDE para ter hot-reload, breakpoints e logs diretos.

## Estrutura da pasta `docker/`

- [`docker/mongo/README.md`](mongo/README.md)
- [`docker/ministack/README.md`](ministack/README.md)
- [`docker/wiremock/README.md`](wiremock/README.md)
- [`docker/otel-collector/README.md`](otel-collector/README.md)
- [`docker/prometheus/README.md`](prometheus/README.md)
- [`docker/tempo/README.md`](tempo/README.md)
- [`docker/loki/README.md`](loki/README.md)
- [`docker/promtail/README.md`](promtail/README.md)
- [`docker/grafana/README.md`](grafana/README.md)

## Pre-requisitos

- Docker Desktop em execucao
- Docker Compose (plugin `docker compose`)
- Portas livres no host: `27017`, `8081`, `4566`, `8082`, `4317`, `4318`, `9090`, `3200`, `3100`, `3000`

## Como subir e parar

No diretorio raiz do repositorio:

**Subir apenas infraestrutura + observabilidade** (fluxo padrao de desenvolvimento):

```powershell
docker compose -f docker/docker-compose.yml up -d
```

**Subir tambem o `security-server` em container** (quando precisar do fluxo OAuth2/login completo sem IDE):

```powershell
docker compose -f docker/docker-compose.yml --profile app-container up -d
```

**Parar e remover containers/rede:**

```powershell
docker compose -f docker/docker-compose.yml down
```

**Reset completo — remove containers e todos os dados locais:**

```powershell
docker compose -f docker/docker-compose.yml down -v
```

**Remover volumes individualmente** (sem derrubar containers):

```powershell
docker volume rm docker_mongo_data    # dados MongoDB
docker volume rm docker_loki_data     # logs indexados
docker volume rm docker_tempo_data    # traces
docker volume rm docker_grafana_data  # estado do Grafana
```

## Endpoints uteis

**Infraestrutura base:**
- Mongo Express (UI do banco): `http://localhost:8081` — usuario `admin`, senha `secret`
- Wiremock (mocks de API): `http://localhost:8082`
- MiniStack (AWS local): `http://localhost:4566/_ministack/health`

**Observabilidade:**
- Grafana (dashboards): `http://localhost:3000`
- Prometheus (metricas brutas): `http://localhost:9090`
- Loki API: `http://localhost:3100`
- Tempo API: `http://localhost:3200`

**Aplicacoes (somente com `--profile app-container`):**
- Security Server (OAuth2/auth): `http://localhost:9001`

## Fluxo de observabilidade (visao geral)

1. Aplicacoes enviam OTLP para `otel-collector` (`4317`/`4318`).
2. `otel-collector` exporta traces para `tempo` e metrics para `prometheus`.
3. `promtail` coleta logs de containers Docker e logs em arquivo do host.
4. `promtail` envia logs para `loki`.
5. `grafana` consulta `prometheus`, `loki` e `tempo` com datasources pre-provisionados.

## Dicas rapidas de troubleshooting

```powershell
docker compose -f docker/docker-compose.yml ps
```

```powershell
docker compose -f docker/docker-compose.yml logs -f grafana
```

```powershell
docker compose -f docker/docker-compose.yml logs -f otel-collector
```

```powershell
docker compose -f docker/docker-compose.yml logs -f promtail
```

