# Skill Implementacao - Nova funcionalidade ou alteracao

## Entrada
- Descricao da mudanca
- Contexto de dominio
- Arquivos alvo (se houver)

## Processo minimo
1. Mapear regra de negocio no dominio (entidade, factory, VO).
2. Definir ou ajustar caso de uso na aplicacao.
3. Definir ou ajustar portas (`in` e `out`) sem vazar infraestrutura.
4. Implementar adaptadores somente se necessario.
5. Reusar classes de `shared`.
6. Manter consistencia com padrao existente do modulo.

## Padrao de implementacao
- Preferir fluxo funcional com `Result` (`map`, `flatMap`).
- Falhas retornam `DomainError` agregavel.
- IDs e validacoes seguem VO/factory do dominio.
- Nomes curtos e autoexplicativos.
- Evitar metodos longos; extrair funcoes de suporte.

## Entrega
- Explicar rapidamente o que mudou e por que.
- Listar riscos ou regressao potencial.
- Exibir conteudo final antes de persistir arquivos.
