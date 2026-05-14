# Wiremock

Mock server para simular APIs externas consumidas localmente.

## Estrutura da pasta

- `mappings/`: regras de request -> response
- `__files/`: payloads JSON retornados pelas regras

## Mocks incluidos

- `GET /distribution/channels/mobile_acmeapp`
- `GET /distribution/journeys/journey_vendaavulsaacme`

## Uso no compose

Servico: `wiremock`

- Porta host: `8082` (container `8080`)
- Volume: `./wiremock:/home/wiremock`
- Flags: `--global-response-templating`, `--verbose`

## Teste rapido

```powershell
Invoke-WebRequest -Uri "http://localhost:8082/distribution/channels/mobile_acmeapp" | Select-Object -ExpandProperty Content
```

```powershell
Invoke-WebRequest -Uri "http://localhost:8082/distribution/journeys/journey_vendaavulsaacme" | Select-Object -ExpandProperty Content
```

