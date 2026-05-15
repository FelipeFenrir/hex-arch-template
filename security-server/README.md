# Security Server

Servidor de autenticacao/autorizacao baseado em Spring Authorization Server, seguindo abordagem hexagonal.

## Modulos

- `modules/domain`: regras de negocio (`User`, `Client`, `Tenant`).
- `modules/application`: use cases e portas.
- `modules/adapters`: APIs REST, filtros, adaptadores Mongo.
- `modules/bootstrap`: composicao Spring e configuracao de beans.

## Features ativas

- OAuth2 Authorization Server (authorize/token/introspection/consent).
- Multi-tenant por subdominio (`<tenant>.127.0.0.1.nip.io`) para APIs tenant-scoped.
- CRUD de `users` (tenant-scoped).
- CRUD de `tenants` (admin/super-admin token claims).
- Cadastro e listagem de `clients` tenant-scoped.
- OpenAPI via `springdoc-openapi` com interfaces `*Api` separadas dos controllers.

## Seguranca

### Tenant por URL

Para APIs tenant-scoped, o tenant e resolvido automaticamente via subdominio por `TenantIdentifierFilter`.

### Tenant admin APIs

Rotas de `tenants` sao globais e nao dependem do tenant atual da URL.

- filtro: `AdminTokenClaimsFilter`
- exige header `Authorization: Bearer <token>`
- claims aceitas: `ROLE_ADMIN` ou `ROLE_SUPER_ADMIN`

### Headers obrigatorios

APIs expostas exigem:

- `X-Correlation-Id`
- `X-Flow-Id`

## Paginacao (HybridPageRequest + PageResult)

Listagens de `clients`, `users` e `tenants` usam o mesmo padrao do `orderquestionnaire`.

### Parametros suportados

- `mode`: `PAGE` ou `CURSOR`
- `page`: indice da pagina (somente `PAGE`)
- `size`: tamanho da pagina (ambos os modos)
- `cursor`: token de cursor (somente `CURSOR`)
- `sort`: repetivel no formato `campo[,ASC|DESC]`

Exemplos:

- `?mode=PAGE&page=0&size=10&sort=username,ASC`
- `?mode=CURSOR&size=10&cursor=20&sort=id,DESC`

### Resposta

Retorno padrao para colecoes:

- `data`: lista de itens
- `meta`: modo, pagina, tamanho, total, cursor
- `links`: navegacao (`next` e `previous`)

## Endpoints principais

### Clients (tenant-scoped)

- `POST /api/v1/clients`
- `GET /api/v1/clients`

### Users (tenant-scoped)

- `POST /api/v1/users`
- `GET /api/v1/users/{id}`
- `GET /api/v1/users`
- `PUT /api/v1/users/{id}`
- `DELETE /api/v1/users/{id}`

### Tenants (admin/super-admin)

- `POST /api/v1/tenants`
- `GET /api/v1/tenants/{id}`
- `GET /api/v1/tenants`
- `PUT /api/v1/tenants/{id}`
- `DELETE /api/v1/tenants/{id}`

## Executando testes

Use o wrapper Gradle da raiz do workspace:

```powershell
.\gradlew.bat :security-server:modules:adapters:test
.\gradlew.bat :security-server:modules:application:test
.\gradlew.bat :security-server:modules:bootstrap:test
```


