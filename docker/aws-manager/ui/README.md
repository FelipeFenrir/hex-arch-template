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
- `/resources/:service` Browser por servico (com views ricas para `s3`, `sqs`, `sns`)

