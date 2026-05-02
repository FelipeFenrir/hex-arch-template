package com.acme.orderquestionnaire.application.unit.question.view;

import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@DisplayName("QuestionView")
class QuestionViewTest {

    private static final Id CREATED_BY_ID = Id.withId("55555555-5555-5555-5555-555555555555");

    @Test
    @DisplayName("should map all fields from Question to QuestionView correctly")
    void shouldMapQuestionFieldsToView() {
        Question question = Question.rehydrate("question_one", "Age", ParameterizationStatus.ACTIVE, "SKU-1", audit());

        QuestionView view = QuestionView.from(question);

        assertEquals("question_one", view.id());
        assertEquals("Age", view.label());
        assertEquals("ACTIVE", view.status());
        assertEquals("SKU-1", view.salesItemReferenceCode());
        assertEquals(CREATED_BY_ID.stringfyId(), view.createdBy().id());
        assertEquals(LocalDateTime.parse("2026-01-10T09:00:00"), view.createdAt());
    }

    @Test
    @DisplayName("should keep null salesItemReferenceCode when mapping Question to QuestionView")
    void shouldKeepNullSalesItemReferenceCodeWhenMappingQuestion() {
        Question question = Question.rehydrate("question_two", "Name", ParameterizationStatus.DRAFT, null, audit());

        QuestionView view = QuestionView.from(question);

        assertEquals("question_two", view.id());
        assertEquals("Name", view.label());
        assertEquals("DRAFT", view.status());
        assertNull(view.salesItemReferenceCode());
    }

    @Test
    @DisplayName("should throw NullPointerException when trying to map a null Question to QuestionView")
    void shouldThrowNullPointerExceptionWhenQuestionIsNull() {
        assertThrows(NullPointerException.class, () -> QuestionView.from(null));
    }

    @Test
    @DisplayName("should throw NullPointerException when trying to map a Question with null status to QuestionView")
    void shouldThrowNullPointerExceptionWhenQuestionStatusIsNull() {
        Question question = Question.rehydrate("question_three", "Document", null, "SKU-3", audit());

        assertThrows(NullPointerException.class, () -> QuestionView.from(question));
    }

    private static AuditInfo audit() {
        return OrderQuestionnaireAuditFactory.createNew(
                CREATED_BY_ID,
                "REF-1",
                "Test User",
                "test@acme.com",
                LocalDateTime.parse("2026-01-10T09:00:00")
        ).getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error));
    }
}

