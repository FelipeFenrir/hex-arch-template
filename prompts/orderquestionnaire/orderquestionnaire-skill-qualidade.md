# Skill Qualidade - Cobertura e confiabilidade

## Meta obrigatoria
- Cobertura 100% com testes unitarios no escopo alterado.
- BDD cobrindo 100% dos cenarios funcionais definidos para a mudanca.
- Mutacao com PIT respeitando o gate atual: `mutationThreshold = 60`.

## Escopo de testes
- Fluxo feliz.
- Falhas de validacao (`Guard`, comandos invalidos, IDs invalidos).
- Falhas de dependencias e ports.
- Regras condicionais e casos de borda.
- Nao regressao de comportamento existente.

## Regras
- Cada regra de negocio nova deve ter teste.
- Cada erro de dominio esperado deve ter teste.
- Cenarios BDD devem estar em formato Given/When/Then.
- Nao reduzir o `mutationThreshold` atual (60).

## Checklist de aceite
- [ ] Unitarios: 100% no escopo alterado
- [ ] BDD: 100% dos cenarios definidos para a mudanca
- [ ] PIT: threshold atual (>= 60) atendido
- [ ] Sem violacao de arquitetura hexagonal
- [ ] Sem duplicacao de utilitarios de `shared`

## Comandos de verificacao (Windows)
```powershell
.\gradlew.bat clean test
.\gradlew.bat :modules:domain:orderquestionnaire:pitest
```
