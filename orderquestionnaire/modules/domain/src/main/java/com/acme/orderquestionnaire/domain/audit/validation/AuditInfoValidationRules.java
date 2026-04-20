package com.acme.orderquestionnaire.domain.audit.validation;

import com.acme.orderquestionnaire.domain.question.errors.QuestionDomainErrors;
import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.shared.pattern.result.validation.DomainRule;
import com.acme.shared.vo.Id;

import java.time.LocalDateTime;
import java.util.List;

public final class AuditInfoValidationRules {

    private AuditInfoValidationRules() {
        throw new IllegalStateException("Utility class");
    }

    public static DomainRule<Id> userIdForQuestionnaire() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(Id candidate) {
                return candidate != null;
            }

            @Override
            public com.acme.shared.pattern.result.DomainError toDomainError() {
                return QuestionnaireDomainErrors.invalidUserId();
            }
        };
    }

    public static DomainRule<String> userNameForQuestionnaire() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(String candidate) {
                return candidate != null && !candidate.isBlank();
            }

            @Override
            public com.acme.shared.pattern.result.DomainError toDomainError() {
                return QuestionnaireDomainErrors.invalidUserName();
            }
        };
    }

    public static DomainRule<LocalDateTime> createdAtForQuestionnaire() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(LocalDateTime candidate) {
                return candidate != null;
            }

            @Override
            public com.acme.shared.pattern.result.DomainError toDomainError() {
                return QuestionnaireDomainErrors.invalidCreatedAt();
            }
        };
    }

    public static DomainRule<LocalDateTime> updatedAtForQuestionnaire() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(LocalDateTime candidate) {
                return candidate != null;
            }

            @Override
            public com.acme.shared.pattern.result.DomainError toDomainError() {
                return QuestionnaireDomainErrors.invalidUpdatedAt();
            }
        };
    }

    public static DomainRule<Id> userIdForQuestion() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(Id candidate) {
                return candidate != null;
            }

            @Override
            public com.acme.shared.pattern.result.DomainError toDomainError() {
                return QuestionDomainErrors.invalidUserId();
            }
        };
    }

    public static DomainRule<String> userNameForQuestion() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(String candidate) {
                return candidate != null && !candidate.isBlank();
            }

            @Override
            public com.acme.shared.pattern.result.DomainError toDomainError() {
                return QuestionDomainErrors.invalidUserName();
            }
        };
    }

    public static DomainRule<LocalDateTime> createdAtForQuestion() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(LocalDateTime candidate) {
                return candidate != null;
            }

            @Override
            public com.acme.shared.pattern.result.DomainError toDomainError() {
                return QuestionDomainErrors.invalidCreatedAt();
            }
        };
    }

    public static DomainRule<LocalDateTime> updatedAtForQuestion() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(LocalDateTime candidate) {
                return candidate != null;
            }

            @Override
            public com.acme.shared.pattern.result.DomainError toDomainError() {
                return QuestionDomainErrors.invalidUpdatedAt();
            }
        };
    }

    public static List<DomainRule<Id>> questionnaireUserIdRules() {
        return List.of(userIdForQuestionnaire());
    }

    public static List<DomainRule<String>> questionnaireUserNameRules() {
        return List.of(userNameForQuestionnaire());
    }

    public static List<DomainRule<LocalDateTime>> questionnaireCreatedAtRules() {
        return List.of(createdAtForQuestionnaire());
    }

    public static List<DomainRule<LocalDateTime>> questionnaireUpdatedAtRules() {
        return List.of(updatedAtForQuestionnaire());
    }

    public static List<DomainRule<Id>> questionUserIdRules() {
        return List.of(userIdForQuestion());
    }

    public static List<DomainRule<String>> questionUserNameRules() {
        return List.of(userNameForQuestion());
    }

    public static List<DomainRule<LocalDateTime>> questionCreatedAtRules() {
        return List.of(createdAtForQuestion());
    }

    public static List<DomainRule<LocalDateTime>> questionUpdatedAtRules() {
        return List.of(updatedAtForQuestion());
    }
}

