package com.acme.orderquestionnaire.application.questionnaire.error;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.ErrorCatalog;

import java.util.Locale;

public enum QuestionnaireErrors implements ErrorCatalog {
    INVALID_COMMAND("INVALID_COMMAND", "command must not be null"),
    INVALID_IDS("INVALID_IDS", "ids must not be null or empty"),
    INVALID_ID("INVALID_ID", "id must not be null or blank"),
    INVALID_CHANNEL_DISTRIBUTION_ID("INVALID_CHANNEL_DISTRIBUTION_ID", "channelDistributionId must not be null or blank"),
    INVALID_JOURNEY_DISTRIBUTION_ID("INVALID_JOURNEY_DISTRIBUTION_ID", "journeyDistributionId must not be null or blank"),
    INVALID_ANSWERS("INVALID_ANSWERS", "answers must not be null"),
    INVALID_DESCRIPTION("INVALID_DESCRIPTION", "description must not be null or blank"),
    INVALID_CREATED_AT("INVALID_CREATED_AT", "createdAt must not be null"),
    INVALID_CREATED_BY("INVALID_CREATED_BY", "createdBy must not be null"),
    INVALID_UPDATED_AT("INVALID_UPDATED_AT", "updatedAt must not be null"),
    INVALID_UPDATED_BY("INVALID_UPDATED_BY", "updatedBy must not be null"),
    INVALID_USER_ID("INVALID_USER_ID", "user id must not be null"),
    INVALID_USER_NAME("INVALID_USER_NAME", "user name must not be null or blank"),
    CHANNEL_DISTRIBUTION_NOT_FOUND("CHANNEL_DISTRIBUTION_NOT_FOUND", "channel distribution with id '%s' was not found"),
    JOURNEY_DISTRIBUTION_NOT_FOUND("JOURNEY_DISTRIBUTION_NOT_FOUND", "journey distribution with id '%s' was not found"),
    QUESTIONNAIRE_ALREADY_EXISTS("QUESTIONNAIRE_ALREADY_EXISTS", "a questionnaire with id '%s' already exists"),
    QUESTIONNAIRE_NOT_FOUND("QUESTIONNAIRE_NOT_FOUND", "questionnaire '%s' was not found"),
    CONDITION_QUESTION_NOT_FOUND("CONDITION_QUESTION_NOT_FOUND",
            "condition references question id '%s' which is not present in the questionnaire"),
    QUESTIONNAIRE_UPDATE_NOT_ALLOWED("QUESTIONNAIRE_UPDATE_NOT_ALLOWED",
            "questionnaire can only be structurally updated while in DRAFT or INACTIVE"),
    QUESTIONNAIRE_DELETE_NOT_ALLOWED("QUESTIONNAIRE_DELETE_NOT_ALLOWED",
            "questionnaire '%s' cannot be deleted with status '%s'; only DRAFT or INACTIVE are allowed"),
    QUESTIONNAIRE_DELETE_FAILED("QUESTIONNAIRE_DELETE_FAILED",
            "failed to delete questionnaire '%s': %s");

    private final String code;
    private final String messageTemplate;

    QuestionnaireErrors(String code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    @Override
    public DomainError toDomainError() {
        return new DomainError(code, messageTemplate);
    }

    @Override
    public DomainError toDomainError(Object... args) {
        return new DomainError(code, String.format(Locale.ROOT, messageTemplate, args));
    }
}
