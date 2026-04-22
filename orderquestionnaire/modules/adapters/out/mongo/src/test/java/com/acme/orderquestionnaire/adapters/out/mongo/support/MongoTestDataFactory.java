package com.acme.orderquestionnaire.adapters.out.mongo.support;

import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditInfo;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditUser;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.AuditUser;
import com.acme.shared.vo.Id;

import java.time.LocalDateTime;

public final class MongoTestDataFactory {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 4, 19, 10, 0, 0);
    private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 4, 19, 10, 30, 0);

    private MongoTestDataFactory() {
    }

    public static AuditUser createdByUser() {
        return new OrderQuestionnaireAuditUser(
                Id.withId("11111111-1111-1111-1111-111111111111"),
                "creator_ref",
                "Creator User",
                "creator@acme.com"
        );
    }

    public static AuditUser updatedByUser() {
        return new OrderQuestionnaireAuditUser(
                Id.withId("22222222-2222-2222-2222-222222222222"),
                "updater_ref",
                "Updater User",
                "updater@acme.com"
        );
    }

    public static AuditInfo createdAuditInfo() {
        return new OrderQuestionnaireAuditInfo(createdByUser(), CREATED_AT, null, null);
    }

    public static AuditInfo updatedAuditInfo() {
        return new OrderQuestionnaireAuditInfo(createdByUser(), CREATED_AT, updatedByUser(), UPDATED_AT);
    }

    public static Question newQuestion(String id) {
        return QuestionFactory.createNew(id, "Question " + id, createdAuditInfo())
                .flatMap(builder -> builder.withSalesItemReferenceCode("sales_item_code_1").build())
                .getOrElseThrow(errors -> new IllegalStateException("Invalid fixture question: " + errors));
    }

    public static Question rehydratedActiveQuestion(String id) {
        return QuestionFactory.rehydrate(id, "Question " + id, ParameterizationStatus.ACTIVE, updatedAuditInfo())
                .flatMap(builder -> builder.withSalesItemReferenceCode("sales_item_code_2").build())
                .getOrElseThrow(errors -> new IllegalStateException("Invalid fixture rehydrated question: " + errors));
    }

    public static Questionnaire newQuestionnaire(String id, String channelId, String journeyId) {
        return QuestionnaireFactory.createNew(id, channelId, journeyId, "Questionnaire " + id, createdAuditInfo())
                .flatMap(QuestionnaireFactory.QuestionnaireBuilder::build)
                .getOrElseThrow(errors -> new IllegalStateException("Invalid fixture questionnaire: " + errors));
    }

    public static Questionnaire rehydratedActiveQuestionnaire(String id, String channelId, String journeyId) {
        return QuestionnaireFactory.rehydrate(id, channelId, journeyId, "Questionnaire " + id,
                        ParameterizationStatus.ACTIVE, updatedAuditInfo())
                .flatMap(QuestionnaireFactory.QuestionnaireBuilder::build)
                .getOrElseThrow(errors -> new IllegalStateException("Invalid fixture rehydrated questionnaire: " + errors));
    }
}

