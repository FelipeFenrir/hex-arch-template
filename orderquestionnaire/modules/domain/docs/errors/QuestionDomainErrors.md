# Implementação de `DomainErrors` em `QuestionDomainErrors`

## Critério de separação

| Classe                      | Responsabilidade                                                        | Onde é usada                                                                                   |
|-----------------------------|-------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| `QuestionDomainErrors`      | Erros de **validação de resposta** (o que o usuário digitou/selecionou) | `AnswerTextStrategy`, `AnswerNumberStrategy`, `AnswerDateStrategy`, `AnswerOptionListStrategy` |

---

## `QuestionDomainErrors` — Focada em respostas

Apenas os erros que dizem respeito à **validação do conteúdo de uma resposta**:

```java
// domain/common/errors/QuestionDomainErrors.java
public final class QuestionDomainErrors {

    public static DomainError invalidAnswerType(String expectedType) { ... }
    public static DomainError patternMismatch(String message) { ... }
    public static DomainError decimalNotAllowed() { ... }
    public static DomainError negativeNotAllowed() { ... }
    public static DomainError valueBelowMin() { ... }
    public static DomainError valueAboveMax() { ... }
    public static DomainError invalidStep(double step) { ... }
    public static DomainError pastDateNotAllowed() { ... }
    public static DomainError invalidOption() { ... }
}
```

---

## Impacto nas classes consumidoras

| Classe                      | Usa                         |
|-----------------------------|-----------------------------|
| `AnswerTextStrategy`        | `QuestionDomainErrors`      |
| `AnswerNumberStrategy`      | `QuestionDomainErrors`      |
| `AnswerDateStrategy`        | `QuestionDomainErrors`      |
| `AnswerOptionListStrategy`  | `QuestionDomainErrors`      |

---

## Lógica de negócio

- **`QuestionDomainErrors`** → "A resposta fornecida é inválida?" → erros de **runtime** (quando o usuário responde)

Essa separação respeita o princípio de responsabilidade única (SRP) e torna mais claro qual domínio gerou cada erro ao processar o `Result`.
