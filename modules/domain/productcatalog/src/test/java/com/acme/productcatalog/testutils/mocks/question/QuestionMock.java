package com.acme.productcatalog.testutils.mocks.question;

import com.acme.productcatalog.domain.product.parameterization.ProductStatus;
import com.acme.productcatalog.domain.question.Question;
import com.acme.shared.stereotypes.test.MockClass;

import java.util.List;

import static com.acme.productcatalog.testutils.mocks.common.enumerator.ParameterizationStatusMock.ACTIVE_STRING;
import static com.acme.productcatalog.testutils.mocks.common.enumerator.ParameterizationStatusMock.DRAFT_STRING;
import static com.acme.productcatalog.testutils.mocks.common.enumerator.ParameterizationStatusMock.INACTIVE_STRING;

@MockClass
public class QuestionMock {
    public static List<Question> DEFAULT_QUESTION_LIST = List.of(
            Question.builder()
                    .withId("acme_test_one")
                    .withLabel("First question of ACME service object")
                    .withStatus(ProductStatus.fromName(ACTIVE_STRING))
                    .build(),
            Question.builder()
                    .withId("acme_test_two")
                    .withLabel("Second question of ACME service object")
                    .withStatus(ProductStatus.fromName(INACTIVE_STRING))
                    .build(),
            Question.builder()
                    .withId("acme_test_three")
                    .withLabel("Third question of ACME service object")
                    .withStatus(ProductStatus.fromName(DRAFT_STRING))
                    .build()
    );

    private QuestionMock() {
    }
}
