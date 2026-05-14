# Mongo

Banco MongoDB local usado pelos modulos da solucao durante desenvolvimento.

## Arquivos desta pasta

- `init.js`: script executado automaticamente no startup do container Mongo.

## O que o `init.js` prepara

- Base `security` com dados iniciais de:
  - `tenants`
  - `clients`
  - `users`
- Base `orderquestionnaire` com dados iniciais de:
  - `channel_distributions`
  - `journey_distributions`
  - `questions`
  - `questionnaires`
  - `questionnaire_questions`

O script usa `updateOne(..., { upsert: true })`, entao os documentos sao criados/atualizados sem duplicacao simples por `_id`.

## Uso no compose

Servico: `mongo`

- Porta host: `27017`
- Volume persistente: `mongo_data:/data/db`
- Script de inicializacao: `./mongo/init.js:/docker-entrypoint-initdb.d/init.js:ro`

## Acesso rapido

- MongoDB: `mongodb://localhost:27017`
- UI (via `mongo-express`): `http://localhost:8081`

