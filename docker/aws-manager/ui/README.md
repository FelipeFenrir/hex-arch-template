# AWS Manager UI

Frontend SPA do `aws-manager`, inspirado no layout e navegacao do StackPort.

## Comandos

```powershell
Push-Location "D:\Projetos\hex-arch-template\docker\aws-manager\ui"
npm install
npm run dev
npm run build
Pop-Location
```

## Rotas

- `/` Dashboard
- `/resources` Browser de recursos
- `/resources/:service` Browser por servico (com views ricas para `s3`, `sqs`, `sns`, `dynamodb`)

## UX atual

- Sidebar retratil com preferencia salva em `localStorage`.
- Favoritos com alinhamento consistente nos cards e no seletor de servicos.
- `sqs` e `sns` com fluxo de editar + delete protegido por dupla confirmacao quando ha mensagens/subscriptions ativas.
- Formularios de `sqs`/`sns` usam seletores de recursos (sem digitacao manual de nomes) e editor JSON maior.
- `s3` possui painel de edicao guiado por selecao de bucket (versioning/tags/delete).
- `dynamodb` possui painel de CRUD de tabelas e gerenciamento de itens via JSON.
- O layout de CRUD foi padronizado em secoes (`Create`, `Edit`, `Delete`, `Actions`) para servir de modelo aos proximos recursos do MiniStack.
- O componente reutilizavel fica em `ui/src/components/ResourceCrudTemplate.tsx` e deve ser reutilizado para novos recursos.

