# Implementação de `DomainErrors` em `QuestionnaireDomainErrors`

## Critério de separação

| Classe                      | Responsabilidade                                                        | Onde é usada                                                                                   |
|-----------------------------|-------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| `QuestionnaireDomainErrors` | Erros de **estrutura/configuração** do questionário                     | `ConfiguredQuestion`, `ConfiguredQuestionFactory`, `QuestionnaireFactory`                      |

---

## `QuestionnaireDomainErrors` — Focada em configuração

Agrupa os erros que dizem respeito à **montagem e integridade do questionário**:

```java
// domain/common/errors/QuestionnaireDomainErrors.java
public final class QuestionnaireDomainErrors {

    // Obrigatoriedade de campos de texto (id, description, etc.)
    public static DomainError requiredField(String fieldName) { ... }

    // Obrigatoriedade de objetos (question, answerConfiguration, etc.)
    public static DomainError requiredObject(String fieldName) { ... }

    // Pergunta obrigatória sem resposta (ConfiguredQuestion.validate)
    public static DomainError mandatoryAnswer(String label) { ... }
}
```

---

## Impacto nas classes consumidoras

| Classe                      | Usa                         |
|-----------------------------|-----------------------------|
| `ConfiguredQuestion`        | `QuestionnaireDomainErrors` |
| `ConfiguredQuestionFactory` | `QuestionnaireDomainErrors` |
| `QuestionnaireFactory`      | `QuestionnaireDomainErrors` |

---

## Lógica de negócio

- **`QuestionnaireDomainErrors`** → "O questionário foi montado corretamente?" → erros de **design-time** (quando o desenvolvedor configura o questionário)

Essa separação respeita o princípio de responsabilidade única (SRP) e torna mais claro qual domínio gerou cada erro ao processar o `Result`.
