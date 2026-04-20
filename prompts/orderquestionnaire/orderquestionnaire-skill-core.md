# Skill Core - orderquestionnaire

## Objetivo
Implementar ou alterar funcionalidades no modulo `orderquestionnaire` mantendo arquitetura hexagonal e baixo acoplamento.

## Regras obrigatorias
- Dominio (`modules/domain/*`): sem dependencia de framework.
- Aplicacao (`modules/application/*`): orquestra casos de uso e portas.
- Adaptadores: apenas infraestrutura e integracao.
- Validacoes de negocio: usar `Result<V,E>`, `Guard` e `DomainError`.
- Marcar intencao com estereotipos de `modules/shared` (`@UseCase`, `@OutputPort`, etc.).
- Reusar utilitarios existentes antes de criar novos:
  - `Result`, `Guard`, `DomainError`
  - `HeaderConstants`
  - `TenantContextHolder`
  - suportes ja existentes no modulo

## Anti-padroes
- Framework no dominio.
- Regra de negocio em adapter.
- Duplicar utilitario ja existente em `shared`.
- Metodo verboso sem extrair responsabilidade.

## Forma de resposta esperada
- Resposta curta e objetiva.
- Plano em checklist.
- Referenciar arquivos e simbolos afetados.
- Mostrar proposta de conteudo/patch antes de salvar arquivo.
