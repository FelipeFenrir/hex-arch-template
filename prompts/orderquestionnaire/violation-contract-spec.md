# Especificação de Contrato: Violations em Validação de Respostas

**Data:** 2026-04-02  
**Módulo:** `modules/application/orderquestionnaire`  
**Use Case:** `ValidateQuestionnaireAnswersService`  
**Versão:** 1.0 (specification for implementation)

---

## 1. Visão Geral

Quando um consumidor submete respostas a um questionário ativo, o serviço de validação retorna um `Result<ValidateQuestionnaireAnswersResponse, ValidationError>`.

Em caso de falha, a resposta contém um array de **violations**, cada uma descrevendo:
- Qual pergunta foi violada
- Qual foi a resposta fornecida
- Qual regra foi violada e por quê
- Metadados completos da regra (tipo, atributos, caminho lógico)

---

## 2. Estrutura de Violation (DTO)

```java
public class QuestionAnswerViolationView {
    private String questionId;              // ex: "income"
    private String questionLabel;           // ex: "Renda mensal"
    private Object providedAnswer;          // ex: -10, null, "invalid_option"
    private String code;                    // ex: "ANSWER_OUT_OF_RANGE"
    private String message;                 // ex: "Valor deve estar entre 0 e 100000"
    private String source;                  // "ANSWER_CONFIGURATION" | "QUESTION_CONDITION" | "QUESTION_STATUS"
    private String ruleType;                // ex: "NUMBER_RANGE", "COMPOSITE_AND", etc
    private Map<String, Object> ruleAttributes; // ex: { "min": 0, "max": 100000, ...}
    private String rulePath;                // ex: "answerConfiguration" | "rootCondition.children[1]"
    private List<String> referencedQuestionIds; // null ou [id1, id2] se applicable
    private Map<String, String> referencedQuestionStatuses; // null ou {"id1": "DRAFT", ...}
}
```

---

## 3. Catálogo de Códigos (code) e RuleType

### 3.1 ANSWER_CONFIGURATION Violations

Origem: `answerConfiguration.validate(value)` retorna erros.

#### 3.1.1 TEXT

| code | ruleType | Quando | ruleAttributes |
|------|----------|--------|-----------------|
| `ANSWER_REQUIRED` | `TEXT_REQUIRED` | Resposta null/vazia obrigatória | `{ "required": true }` |
| `ANSWER_LENGTH_INVALID` | `TEXT_LENGTH` | Tamanho fora do intervalo | `{ "minLength": 5, "maxLength": 100, "actualLength": 3 }` |
| `ANSWER_PATTERN_INVALID` | `TEXT_PATTERN` | Não atende regex/padrão | `{ "pattern": "^[A-Z]+$", "providedValue": "abc" }` |
| `ANSWER_VALUE_INVALID` | `TEXT_FORMAT` | Formato inválido (JSON, etc) | `{ "expectedFormat": "json" }` |

#### 3.1.2 NUMBER

| code | ruleType | Quando | ruleAttributes |
|------|----------|--------|-----------------|
| `ANSWER_REQUIRED` | `NUMBER_REQUIRED` | Resposta null obrigatória | `{ "required": true }` |
| `ANSWER_TYPE_INVALID` | `NUMBER_TYPE` | Não é número | `{ "expectedType": "number", "providedType": "string" }` |
| `ANSWER_OUT_OF_RANGE` | `NUMBER_RANGE` | Fora de min/max | `{ "min": 0, "max": 100000, "inclusiveMin": true, "inclusiveMax": true, "providedValue": -10 }` |

#### 3.1.3 DATE

| code | ruleType | Quando | ruleAttributes |
|------|----------|--------|-----------------|
| `ANSWER_REQUIRED` | `DATE_REQUIRED` | Resposta null obrigatória | `{ "required": true }` |
| `ANSWER_TYPE_INVALID` | `DATE_TYPE` | Não é data válida | `{ "expectedFormat": "yyyy-MM-dd", "providedValue": "invalid" }` |
| `ANSWER_DATE_OUT_OF_RANGE` | `DATE_RANGE` | Fora de intervalo | `{ "minDate": "2020-01-01", "maxDate": "2026-04-02", "providedValue": "2010-01-01" }` |

#### 3.1.4 OPTION_LIST

| code | ruleType | Quando | ruleAttributes |
|------|----------|--------|-----------------|
| `ANSWER_REQUIRED` | `OPTION_LIST_REQUIRED` | Resposta null obrigatória | `{ "required": true }` |
| `ANSWER_OPTION_INVALID` | `OPTION_LIST_INVALID` | Opção não existe | `{ "validOptions": ["OPT_A", "OPT_B"], "providedValue": "OPT_C", "optionCount": 2 }` |

---

### 3.2 QUESTION_CONDITION Violations

Origem: `rootCondition` referencia pergunta inativa ou retorna falso sem razão clara.

#### 3.2.1 Condition Simples

| code | ruleType | Quando | ruleAttributes |
|------|----------|--------|-----------------|
| `CONDITION_REFERENCED_QUESTION_NOT_ACTIVE` | `VISIBILITY_CONDITION` \| `EQUAL_CONDITION` \| `NUMERIC_CONDITION` | Pergunta referenciada inativa | `{ "conditionType": "EQUAL", "expectedValue": "A", "referencedQuestionId": "type", "referencedQuestionStatus": "DRAFT" }` |

#### 3.2.2 Condition Composta (CompositeCondition)

| code | ruleType | Quando | ruleAttributes |
|------|----------|--------|-----------------|
| `CONDITION_REFERENCED_QUESTION_NOT_ACTIVE` | `COMPOSITE_AND` | Qualquer sub-condição referencia pergunta inativa | `{ "operator": "AND", "childCount": 2, "inactiveQuestionReferences": ["income_source"], "inactiveQuestionStatuses": { "income_source": "DRAFT" } }` |
| `CONDITION_REFERENCED_QUESTION_NOT_ACTIVE` | `COMPOSITE_OR` | Qualquer sub-condição referencia pergunta inativa | `{ "operator": "OR", "childCount": 3, "inactiveQuestionReferences": ["type1", "type2"], "inactiveQuestionStatuses": { "type1": "INACTIVE", "type2": "DRAFT" } }` |

---

### 3.3 QUESTION_STATUS Violations

Origem: `question.status() != ACTIVE`.

| code | ruleType | Quando | ruleAttributes |
|------|----------|--------|-----------------|
| `QUESTION_NOT_ACTIVE` | `QUESTION_STATUS` | Pergunta inativa mas resposta foi fornecida | `{ "currentStatus": "DRAFT", "expectedStatus": "ACTIVE" }` |

---

## 4. Exemplos de Payloads

### 4.1 Validação OUT_OF_RANGE em NUMBER

```json
{
  "questionId": "income",
  "questionLabel": "Renda mensal",
  "providedAnswer": -10,
  "code": "ANSWER_OUT_OF_RANGE",
  "message": "Valor deve estar entre 0 e 100000",
  "source": "ANSWER_CONFIGURATION",
  "ruleType": "NUMBER_RANGE",
  "ruleAttributes": {
    "min": 0,
    "max": 100000,
    "inclusiveMin": true,
    "inclusiveMax": true,
    "providedValue": -10
  },
  "rulePath": "answerConfiguration"
}
```

### 4.2 Pergunta Inativa com Resposta Fornecida

```json
{
  "questionId": "legacy_field",
  "questionLabel": "Campo legado",
  "providedAnswer": "some_value",
  "code": "QUESTION_NOT_ACTIVE",
  "message": "Pergunta não está ativa para receber respostas",
  "source": "QUESTION_STATUS",
  "ruleType": "QUESTION_STATUS",
  "ruleAttributes": {
    "currentStatus": "DRAFT",
    "expectedStatus": "ACTIVE"
  },
  "rulePath": "question"
}
```

### 4.3 Condition Simples com Pergunta Referenciada Inativa

```json
{
  "questionId": "income_type",
  "questionLabel": "Tipo de renda",
  "providedAnswer": null,
  "code": "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE",
  "message": "A condição referencia pergunta inativa: employment_status",
  "source": "QUESTION_CONDITION",
  "ruleType": "EQUAL_CONDITION",
  "ruleAttributes": {
    "conditionType": "EQUAL",
    "expectedValue": "employed",
    "referencedQuestionId": "employment_status",
    "referencedQuestionStatus": "DRAFT"
  },
  "rulePath": "rootCondition",
  "referencedQuestionIds": ["employment_status"],
  "referencedQuestionStatuses": {
    "employment_status": "DRAFT"
  }
}
```

### 4.4 Condition Composta (AND) com Pergunta Inativa

```json
{
  "questionId": "bonus",
  "questionLabel": "Bônus",
  "providedAnswer": null,
  "code": "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE",
  "message": "A condição AND referencia perguntas inativas: performance_level",
  "source": "QUESTION_CONDITION",
  "ruleType": "COMPOSITE_AND",
  "ruleAttributes": {
    "operator": "AND",
    "childCount": 2,
    "inactiveQuestionReferences": ["performance_level"],
    "inactiveQuestionStatuses": {
      "performance_level": "INACTIVE"
    }
  },
  "rulePath": "rootCondition.children[1]",
  "referencedQuestionIds": ["is_eligible", "performance_level"],
  "referencedQuestionStatuses": {
    "performance_level": "INACTIVE"
  }
}
```

### 4.5 Condition Composta (OR) com Múltiplas Perguntas Inativas

```json
{
  "questionId": "approval_needed",
  "questionLabel": "Aprovação necessária",
  "providedAnswer": null,
  "code": "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE",
  "message": "A condição OR referencia perguntas inativas: manager_approval, director_approval",
  "source": "QUESTION_CONDITION",
  "ruleType": "COMPOSITE_OR",
  "ruleAttributes": {
    "operator": "OR",
    "childCount": 3,
    "inactiveQuestionReferences": ["manager_approval", "director_approval"],
    "inactiveQuestionStatuses": {
      "manager_approval": "DRAFT",
      "director_approval": "DRAFT"
    }
  },
  "rulePath": "rootCondition",
  "referencedQuestionIds": ["is_high_value", "manager_approval", "director_approval"],
  "referencedQuestionStatuses": {
    "manager_approval": "DRAFT",
    "director_approval": "DRAFT"
  }
}
```

### 4.6 OPTION_LIST com Opção Inválida

```json
{
  "questionId": "payment_method",
  "questionLabel": "Método de pagamento",
  "providedAnswer": "CREDIT_CARD_OLD",
  "code": "ANSWER_OPTION_INVALID",
  "message": "Opção selecionada é inválida",
  "source": "ANSWER_CONFIGURATION",
  "ruleType": "OPTION_LIST_INVALID",
  "ruleAttributes": {
    "providedValue": "CREDIT_CARD_OLD",
    "validOptions": ["CREDIT_CARD", "DEBIT_CARD", "PIX"],
    "optionCount": 3
  },
  "rulePath": "answerConfiguration.options[2]"
}
```

---

## 5. Mapeamento de Fluxo de Validação

```
Questionnaire.answerValidation(answers: Map)
  ↓
  for each ConfiguredQuestion (in order):
    ↓
    1. Check question.status() != ACTIVE
       → if provided answer: generate QUESTION_NOT_ACTIVE violation
       → if no answer: skip (success)
    ↓
    2. Check rootCondition visibility + referenced questions status
       → if ANY referenced question != ACTIVE: 
         generate CONDITION_REFERENCED_QUESTION_NOT_ACTIVE violation
    ↓
    3. Check answerConfiguration.validate(value)
       → if errors: generate violations with ANSWER_* codes
  ↓
  Return Result<Void, List<Violation>>
```

---

## 6. Critérios de Aceite (Test Scenarios)

- [ ] Pergunta inativa com resposta fornecida → `QUESTION_NOT_ACTIVE`
- [ ] Pergunta inativa sem resposta → sucesso (nenhuma violação)
- [ ] Condition simples com pergunta referenciada inativa → `CONDITION_REFERENCED_QUESTION_NOT_ACTIVE`
- [ ] Condition composta AND com qualquer sub-referência inativa → `CONDITION_REFERENCED_QUESTION_NOT_ACTIVE` + `COMPOSITE_AND`
- [ ] Condition composta OR com múltiplas sub-referências inativas → `CONDITION_REFERENCED_QUESTION_NOT_ACTIVE` + `COMPOSITE_OR`
- [ ] AnswerConfiguration TEXT com length inválido → `ANSWER_LENGTH_INVALID` + `TEXT_LENGTH`
- [ ] AnswerConfiguration NUMBER com valor out of range → `ANSWER_OUT_OF_RANGE` + `NUMBER_RANGE`
- [ ] AnswerConfiguration OPTION_LIST com opção inválida → `ANSWER_OPTION_INVALID` + `OPTION_LIST_INVALID`
- [ ] Múltiplas violations na mesma resposta → array com todas
- [ ] Múltiplas violations em múltiplas perguntas → array consolidado

---

## 7. Dependências de Implementação

### Domain Layer
- `ConfiguredQuestion.validate(answers)` → verificar status + condition
- `Questionnaire.answerValidation(answers)` → orquestrar validação de todas perguntas
- `QuestionCondition.referencedQuestionIds()` → extrair IDs referenciados (já existe)
- Novos `DomainError` em `QuestionnaireDomainErrors` e `QuestionDomainErrors`

### Application Layer
- `ValidateQuestionnaireAnswersService` → mapear resultado de domínio para `QuestionAnswerViolationView`
- `QuestionAnswerViolationView` → DTO com campos de contrato

### Tests
- Unit: cenários por tipo de violation
- BDD (Gherkin): fluxos end-to-end
- Functional: ponta a ponta no módulo application

---

## 8. Notas de Implementação

1. **Backward Compatibility**: Este é um novo contrato de retorno. Se houver consumidor atual, verificar breaking changes.
2. **Composites com Nesting Profundo**: Para `rulePath`, manter contagem exata de caminho (ex: `rootCondition.children[2].children[0]`).
3. **Performance**: Validar perguntas em ordem; se houver muitas violations, considerar batch processing.
4. **i18n**: `message` pode ser traduzido na application layer baseado em Locale; `code` permanece invariante.


