package com.acme.orderquestionnaire.application.question.error;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.ErrorCatalog;

import java.util.Locale;

public enum QuestionErrors implements ErrorCatalog {
    INVALID_COMMAND("INVALID_COMMAND", "command must not be null"),
    INVALID_ID("INVALID_ID", "id must not be null or blank"),
    INVALID_STATUS("INVALID_STATUS", "status is invalid"),
    INVALID_IDS("INVALID_IDS", "ids must not be null or empty"),
    INVALID_CREATED_AT("INVALID_CREATED_AT", "createdAt must not be null"),
    INVALID_CREATED_BY("INVALID_CREATED_BY", "createdBy must not be null"),
    INVALID_UPDATED_AT("INVALID_UPDATED_AT", "updatedAt must not be null"),
    INVALID_UPDATED_BY("INVALID_UPDATED_BY", "updatedBy must not be null"),
    INVALID_USER_ID("INVALID_USER_ID", "user id must not be null or blank"),
    INVALID_USER_NAME("INVALID_USER_NAME", "user name must not be null or blank"),
    QUESTION_ALREADY_EXISTS("QUESTION_ALREADY_EXISTS", "a question with id '%s' already exists"),
    INVALID_STATUS_TRANSITION("INVALID_STATUS_TRANSITION", "Cannot transition from '%s' to '%s'"),
    QUESTION_NOT_FOUND("QUESTION_NOT_FOUND", "question was not found"),
    QUESTION_IN_USE("QUESTION_IN_USE", "question '%s' is referenced by questionnaires: %s"),
    QUESTION_DELETE_FAILED("QUESTION_DELETE_FAILED", "failed to delete question '%s': %s"),
    CREATE_NOT_IMPLEMENTED("NOT_IMPLEMENTED", "createQuestion is not implemented yet"),
    UPDATE_NOT_IMPLEMENTED("NOT_IMPLEMENTED", "updateQuestion is not implemented yet"),
    INACTIVATE_NOT_IMPLEMENTED("NOT_IMPLEMENTED", "inactivateQuestion is not implemented yet"),
    INACTIVATE_BULK_NOT_IMPLEMENTED("NOT_IMPLEMENTED", "inactivateQuestions is not implemented yet"),
    ACTIVATE_NOT_IMPLEMENTED("NOT_IMPLEMENTED", "activateQuestion is not implemented yet"),
    ACTIVATE_BULK_NOT_IMPLEMENTED("NOT_IMPLEMENTED", "activateQuestions is not implemented yet"),
    NOT_IMPLEMENTED_TEMPLATE("NOT_IMPLEMENTED", "%s is not implemented yet");

    private final String code;
    private final String messageTemplate;

    QuestionErrors(String code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    public DomainError toDomainError() {
        return new DomainError(code, messageTemplate);
    }

    public DomainError toDomainError(Object... args) {
        return new DomainError(code, String.format(Locale.ROOT, messageTemplate, args));
    }
}

