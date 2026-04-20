package com.acme.orderquestionnaire.domain.questionnaire.validation;

import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.shared.pattern.result.validation.DomainRule;
import com.acme.shared.pattern.result.DomainError;
import java.util.List;

public final class QuestionnaireIdentityRules {

    private static final String SNAKE_CASE_PATTERN = "^[a-z]+(?:_[a-z0-9]+)*$";

    private QuestionnaireIdentityRules() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Regra: id deve estar presente.
     */
    public static DomainRule<String> questionnaireIdNotNull() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(String candidate) {
                return candidate != null && !candidate.isBlank();
            }

            @Override
            public DomainError toDomainError() {
                return QuestionnaireDomainErrors.requiredField("id");
            }
        };
    }

    /**
     * Regra: id deve estar em formato snake_case.
     */
    public static DomainRule<String> questionnaireIdSnakeCaseFormat() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(String candidate) {
                return candidate == null || candidate.isBlank() || candidate.matches(SNAKE_CASE_PATTERN);
            }

            @Override
            public DomainError toDomainError() {
                return QuestionnaireDomainErrors.invalidIdFormat();
            }
        };
    }

    /**
     * Regra: channelDistributionId deve estar presente.
     */
    public static DomainRule<String> questionnaireChannelNotNull() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(String candidate) {
                return candidate != null && !candidate.isBlank();
            }

            @Override
            public DomainError toDomainError() {
                return QuestionnaireDomainErrors.requiredField("channelDistributionId");
            }
        };
    }

    /**
     * Regra: journeyDistributionId deve estar presente.
     */
    public static DomainRule<String> questionnaireJourneyNotNull() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(String candidate) {
                return candidate != null && !candidate.isBlank();
            }

            @Override
            public DomainError toDomainError() {
                return QuestionnaireDomainErrors.requiredField("journeyDistributionId");
            }
        };
    }

    /**
     * Regra: description deve estar presente.
     */
    public static DomainRule<String> questionnaireDescriptionNotNull() {
        return new DomainRule<>() {
            @Override
            public boolean isSatisfiedBy(String candidate) {
                return candidate != null && !candidate.isBlank();
            }

            @Override
            public DomainError toDomainError() {
                return QuestionnaireDomainErrors.requiredField("description");
            }
        };
    }

    public static List<DomainRule<String>> questionnaireIdRules() {
        return List.of(
                questionnaireIdNotNull(),
                questionnaireIdSnakeCaseFormat()
        );
    }
}

