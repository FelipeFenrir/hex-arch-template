package com.acme.orderquestionnaire.testutils.mocks.questionnaire;

import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.orderquestionnaire.testutils.mocks.audit.AuditTestData;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.stereotypes.test.MockClass;

@MockClass
public class QuestionnaireMock {
    public static final String DEFAULT_QUESTIONNAIRE_ID = "customer_satisfaction_survey";
    public static final String DEFAULT_CHANNEL_CODE = "CUST_SAT_CHANNEL";
    public static final String DEFAULT_JOURNEY_CODE = "CUST_SAT_JOURNEY";
    public static final String DEFAULT_DESCRIPTION = "A survey to measure customer satisfaction levels.";

    public static final Questionnaire DEFAULT_QUESTIONNAIRE = active();

    public static Questionnaire active() {
        return QuestionnaireFactory.rehydrate(
                        DEFAULT_QUESTIONNAIRE_ID,
                        DEFAULT_CHANNEL_CODE,
                        DEFAULT_JOURNEY_CODE,
                        DEFAULT_DESCRIPTION,
                        ParameterizationStatus.ACTIVE,
                        AuditTestData.createdAudit()
                )
                .flatMap(QuestionnaireFactory.QuestionnaireBuilder::build)
                .getOrElseThrow(error -> new IllegalStateException("Invalid mock setup: " + error));
    }

    public static Questionnaire draft() {
        return QuestionnaireFactory.createNew(
                        DEFAULT_QUESTIONNAIRE_ID,
                        DEFAULT_CHANNEL_CODE,
                        DEFAULT_JOURNEY_CODE,
                        DEFAULT_DESCRIPTION,
                        AuditTestData.createdAudit()
                )
                .flatMap(QuestionnaireFactory.QuestionnaireBuilder::build)
                .getOrElseThrow(error -> new IllegalStateException("Invalid mock setup: " + error));
    }

    private QuestionnaireMock() {
        throw new IllegalStateException("Utility class");
    }
}
