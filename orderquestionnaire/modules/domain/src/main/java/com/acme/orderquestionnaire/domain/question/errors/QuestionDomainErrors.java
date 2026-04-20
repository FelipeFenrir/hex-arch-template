package com.acme.orderquestionnaire.domain.question.errors;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.enumerator.ParameterizationStatus;

public final class QuestionDomainErrors {

    private QuestionDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    public static DomainError mandatoryAnswer(String label) {
        return new DomainError("MANDATORY_ANSWER", "Mandatory: " + label);
    }

    public static DomainError invalidAnswerType(String expectedType) {
        return new DomainError("INVALID_ANSWER_TYPE", "Answer must be a " + expectedType + ".");
    }

    public static DomainError patternMismatch(String message) {
        return new DomainError("PATTERN_MISMATCH", message);
    }

    public static DomainError decimalNotAllowed() {
        return new DomainError("DECIMAL_NOT_ALLOWED", "Decimal numbers not allowed.");
    }

    public static DomainError negativeNotAllowed() {
        return new DomainError("NEGATIVE_NOT_ALLOWED", "Negative numbers not allowed.");
    }

    public static DomainError valueBelowMin() {
        return new DomainError("VALUE_BELOW_MIN", "Value below minimum.");
    }

    public static DomainError valueAboveMax() {
        return new DomainError("VALUE_ABOVE_MAX", "Value above maximum.");
    }

    public static DomainError invalidStep(double step) {
        return new DomainError("INVALID_STEP", "The value must respect the step of: " + step);
    }

    public static DomainError pastDateNotAllowed() {
        return new DomainError("PAST_DATE_NOT_ALLOWED", "Past dates are not allowed.");
    }

    public static DomainError invalidOption() {
        return new DomainError("INVALID_OPTION", "Invalid or inactive option.");
    }

    public static DomainError requiredField(String fieldName) {
        return new DomainError("REQUIRED_FIELD", fieldName + " must not be blank");
    }

    public static DomainError requiredObject(String fieldName) {
        return new DomainError("REQUIRED_OBJECT", fieldName + " must not be null");
    }

    public static DomainError invalidIdFormat() {
        return new DomainError("INVALID_ID_FORMAT", "id must be in snake_case format (e.g. my_question_id)");
    }

    public static DomainError invalidStatusTransition(ParameterizationStatus from, ParameterizationStatus to) {
        return new DomainError("INVALID_STATUS_TRANSITION", "Cannot transition from " + from + " to " + to);
    }

    public static DomainError invalidCreatedAt() {
        return new DomainError("INVALID_CREATED_AT", "createdAt must not be null");
    }

    public static DomainError invalidUpdatedAt() {
        return new DomainError("INVALID_UPDATED_AT", "updatedAt must not be null");
    }

    public static DomainError invalidUserId() {
        return new DomainError("INVALID_USER_ID", "user id must not be null or blank");
    }

    public static DomainError invalidUserName() {
        return new DomainError("INVALID_USER_NAME", "user name must not be null or blank");
    }
}
