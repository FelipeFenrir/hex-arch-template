# Template curto para prompt futuro

## Pedido
[descreva a feature ou alteracao em 1 a 3 linhas]

## Contexto
- Modulo alvo: `orderquestionnaire`
- Camadas afetadas: [domain|application|adapters]
- Arquivos ou simbolos de referencia: [lista curta]

## Restricoes obrigatorias
- Arquitetura hexagonal estrita.
- Reusar `modules/shared` antes de criar novo utilitario.
- Usar `Result`, `Guard`, `DomainError`.
- Dominio sem framework.
- Resposta curta e objetiva.
- Apresentar conteudo antes de salvar.

## Testes obrigatorios
- Unitarios com 100% no escopo alterado.
- BDD cobrindo 100% dos cenarios definidos para a mudanca.
- PIT respeitando `mutationThreshold = 60`.

## Saida esperada
1. Plano em checklist.
2. Implementacao proposta.
3. Testes propostos ou implementados.
4. Conteudo final para aprovacao antes de gravar.
