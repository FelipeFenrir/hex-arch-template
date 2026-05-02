package com.acme.orderquestionnaire.domain.unit.testutils.mocks.question;

import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.unit.testutils.mocks.audit.AuditTestData;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.MockClass;

import java.util.List;

@MockClass
public class QuestionMock {

    public static Result<Question, List<DomainError>> DEFAULT_QUESTION_1 = QuestionFactory
            .rehydrate("how_satisfied_are_you", "How satisfied are you with our service?",
                    ParameterizationStatus.ACTIVE, "SALE", AuditTestData.createdAudit())
            .flatMap(builder -> builder.build());

    public static Result<Question, List<DomainError>> DEFAULT_QUESTION_2 = QuestionFactory
            .rehydrate("you_recommend_us", "Would you recommend us to a friend?",
                    ParameterizationStatus.ACTIVE, "SALE", AuditTestData.createdAudit())
            .flatMap(builder -> builder.build());

    private QuestionMock() {
        throw new IllegalStateException("Utility class");
    }
}
