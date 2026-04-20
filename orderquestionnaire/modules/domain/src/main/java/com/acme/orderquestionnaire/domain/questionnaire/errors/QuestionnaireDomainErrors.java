package com.acme.orderquestionnaire.domain.questionnaire.errors;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.enumerator.ParameterizationStatus;

import java.util.Set;

public final class QuestionnaireDomainErrors {

    private QuestionnaireDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    public static DomainError mandatoryAnswer(String label) {
        return new DomainError("MANDATORY_ANSWER", "Mandatory: " + label);
    }

    public static DomainError answerNotAllowedByCondition(String label) {
        return new DomainError("ANSWER_NOT_ALLOWED_BY_CONDITION",
                "Question '" + label + "' is not visible for the current answers and should not be answered.");
    }

    public static DomainError questionNotActive(String label, String currentStatus) {
        return new DomainError("QUESTION_NOT_ACTIVE",
                "Question '" + label + "' is not active (current status: " + currentStatus + ") and should not be answered.");
    }

    public static DomainError conditionReferencedQuestionNotActive(String questionLabel, Set<String> inactiveIds) {
        return new DomainError("CONDITION_REFERENCED_QUESTION_NOT_ACTIVE",
                "Condition for question '" + questionLabel + "' references inactive questions: " + String.join(", ", inactiveIds));
    }

    public static DomainError requiredField(String fieldName) {
        return new DomainError("REQUIRED_FIELD", fieldName + " must not be blank");
    }

    public static DomainError requiredObject(String fieldName) {
        return new DomainError("REQUIRED_OBJECT", fieldName + " must not be null");
    }

    public static DomainError invalidIdFormat() {
        return new DomainError("INVALID_ID_FORMAT", "id must be in snake_case format (e.g. my_questionnaire_id)");
    }

    public static DomainError invalidOrder() {
        return new DomainError("INVALID_ORDER", "order must be greater than or equal to zero");
    }

    public static DomainError invalidStatusTransition(ParameterizationStatus from, ParameterizationStatus to) {
        return new DomainError("INVALID_STATUS_TRANSITION", "Cannot transition from " + from + " to " + to);
    }

    public static DomainError invalidConfiguredQuestionForActivation() {
        return new DomainError("INVALID_CONFIGURED_QUESTION_FOR_ACTIVATION",
                "questionnaire must have at least one configured question with answerConfiguration and order");
    }

    public static DomainError questionnaireUpdateNotAllowed() {
        return new DomainError("QUESTIONNAIRE_UPDATE_NOT_ALLOWED",
                "questionnaire can only be structurally updated while in DRAFT or INACTIVE");
    }

    public static DomainError invalidCreatedAt() {
        return new DomainError("INVALID_CREATED_AT", "createdAt must not be null");
    }

    public static DomainError invalidUpdatedAt() {
        return new DomainError("INVALID_UPDATED_AT", "updatedAt must not be null");
    }

    public static DomainError invalidUserId() {
        return new DomainError("INVALID_USER_ID", "user id must not be null");
    }

    public static DomainError invalidUserName() {
        return new DomainError("INVALID_USER_NAME", "user name must not be null or blank");
    }
}
