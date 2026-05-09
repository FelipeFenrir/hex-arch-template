# Hex-arch-template

Template/playground de arquitetura hexagonal com foco em boundaries claros entre dominio, aplicacao e adapters.

## Visao geral do repositorio

Este repositorio organiza multiplos projetos relacionados e componentes compartilhados.

Escopo desta documentacao (intencionalmente):
- inclui os projetos ativos de aplicacao, libs compartilhadas e ambiente local
- exclui `prompts/`, `scripts/`, `purefilter/` e `modules/` (raiz)

## Mapa de pastas (escopo documentado)

- [`README.md`](README.md): pagina central do repositorio
- [`AGENTS.md`](AGENTS.md): diretrizes de arquitetura e convencoes do workspace
- [`Taskfile.yml`](Taskfile.yml): automacao por aplicacao (`build`, `test`, `docker:build`, `up`, `down`)
- [`settings.gradle`](settings.gradle): composicao Gradle (builds incluidos)
- [`build.gradle`](build.gradle): configuracao Gradle raiz
- [`docker/`](docker/): ambiente local de infraestrutura + observabilidade + profile de apps
- [`api_collection/`](api_collection/): colecoes e definicao OpenAPI para testes manuais
- [`runs/`](runs/): configuracoes de execucao para IDE
- [`orderquestionnaire/`](orderquestionnaire/): aplicacao de questionarios (hexagonal)
- [`security-server/`](security-server/): aplicacao de autenticacao/autorizacao
- [`shared/`](shared/): bibliotecas e utilitarios compartilhados
- [`observability/`](observability/): componentes transversais de observabilidade

## Projetos principais

### `orderquestionnaire/`

Aplicacao principal de questionarios, com foco em:
- dominio e factories em `modules/domain`
- orquestracao de casos de uso em `modules/application`
- adapters in/out em `modules/adapters`
- bootstrap em `modules/bootstrap`

Referencias:
- [`orderquestionnaire/README.md`](orderquestionnaire/README.md)
- [`orderquestionnaire/settings.gradle`](orderquestionnaire/settings.gradle)

### `security-server/`

Aplicacao de seguranca (login, OAuth2, tokens/JWT e componentes correlatos), com organizacao modular semelhante.

Referencias:
- [`security-server/README.md`](security-server/README.md)
- [`security-server/settings.gradle`](security-server/settings.gradle)

### `shared/`

Biblioteca compartilhada com utilitarios e padroes usados entre as aplicacoes.

Referencias:
- [`shared/README.md`](shared/README.md)
- [`shared/docs/`](shared/docs/)

### `observability/`

Biblioteca/componente de observabilidade para logging, metricas e traces.

Referencias:
- [`observability/Readme.md`](observability/Readme.md)

## Ambiente local com Docker

A pasta [`docker/`](docker/) contem o ambiente local para simular infraestrutura e observabilidade de producao para desenvolvimento.

Cobertura:
- infraestrutura base (Mongo, MiniStack, Wiremock)
- stack de observabilidade (OTel Collector, Prometheus, Tempo, Loki, Promtail, Grafana)
- apps opcionais em container via `--profile app-container` (`security-server` e `orderquestionnaire`)

Guia completo:
- [`docker/README.md`](docker/README.md)

## Tasks (resumo)

As automacoes estao separadas por aplicacao no [`Taskfile.yml`](Taskfile.yml).

- `task security-server:build`
- `task security-server:test`
- `task security-server:docker:build`
- `task security-server:up`
- `task security-server:down`

- `task orderquestionnaire:build`
- `task orderquestionnaire:test`
- `task orderquestionnaire:docker:build`
- `task orderquestionnaire:up`
- `task orderquestionnaire:down`

- `task apps:docker:build` (build das duas imagens em sequencia)

## Gradle na raiz (composite build)

O `build.gradle` da raiz atua apenas como **agregador** do monorepo.

- A raiz nao possui subprojetos Gradle proprios.
- Os projetos ativos entram via `includeBuild(...)` no [`settings.gradle`](settings.gradle):
  - `shared`
  - `observability`
  - `security-server`
  - `orderquestionnaire`
- Por isso, os comandos sao executados com prefixo do build incluido, por exemplo:
  - `:shared:build`
  - `:observability:test`
  - `:security-server:build`
  - `:orderquestionnaire:build`

Para visualizar o estado atual da composicao:

```powershell
.\gradlew.bat projects
```

## Execucao rapida

No Windows PowerShell:

```powershell
.\gradlew.bat :orderquestionnaire:test :security-server:test
.\gradlew.bat projects
```

Subir infraestrutura e observabilidade local:

```powershell
docker compose -f docker/docker-compose.yml up -d
```

Subir tambem as apps em container:

```powershell
docker compose -f docker/docker-compose.yml --profile app-container up -d
```
