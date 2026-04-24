package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.AnswerConfigParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConditionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("ResolveConfiguredQuestionsStep")
class ResolveConfiguredQuestionsStepTest {

    @Test
    @DisplayName("should remove and upsert configured questions and store final list in context")
    void shouldResolveConfiguredQuestionsSuccessfully() {
        QuestionCommandOutPort outPort = mock(QuestionCommandOutPort.class);
        Question newQuestion = question("q_new");
        when(outPort.findQuestionById("q_new")).thenReturn(Optional.of(newQuestion));

        Questionnaire existing = questionnaire(List.of(configuredQuestion("q_old")));
        UpdateQuestionnaireCommand command = new UpdateQuestionnaireCommand(
                "q_001",
                "APP",
                "JOURNEY_01",
                null,
                null,
                List.of(new UpdateConfiguredQuestionParam(
                        "q_new",
                        new ConfiguredQuestionParam(1, new AnswerConfigParam.Text(null, null), null)
                )),
                List.of("q_old"),
                null,
                LocalDateTime.parse("2026-01-02T10:00:00")
        );

        ResolveConfiguredQuestionsStep step = new ResolveConfiguredQuestionsStep(outPort);
        UpdateQuestionnairePipelineContext context = new UpdateQuestionnairePipelineContext(command);
        context.existingQuestionnaire(existing);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        assertEquals(1, context.configuredQuestions().size());
        assertEquals("q_new", context.configuredQuestions().getFirst().question().id());
    }

    @Test
    @DisplayName("should fail when question to upsert is not found")
    void shouldFailWhenQuestionToUpsertIsNotFound() {
        QuestionCommandOutPort outPort = mock(QuestionCommandOutPort.class);
        when(outPort.findQuestionById("q_missing")).thenReturn(Optional.empty());

        Questionnaire existing = questionnaire(List.of(configuredQuestion("q_old")));
        UpdateQuestionnaireCommand command = new UpdateQuestionnaireCommand(
                "q_001",
                "APP",
                "JOURNEY_01",
                null,
                null,
                List.of(new UpdateConfiguredQuestionParam(
                        "q_missing",
                        new ConfiguredQuestionParam(1, new AnswerConfigParam.Text(null, null), null)
                )),
                null,
                null,
                LocalDateTime.parse("2026-01-02T10:00:00")
        );

        ResolveConfiguredQuestionsStep step = new ResolveConfiguredQuestionsStep(outPort);
        UpdateQuestionnairePipelineContext context = new UpdateQuestionnairePipelineContext(command);
        context.existingQuestionnaire(existing);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTION_NOT_FOUND")));
    }

    @Test
    @DisplayName("should fail when condition references a question id not present in final questionnaire")
    void shouldFailWhenConditionReferencesUnknownQuestionId() {
        QuestionCommandOutPort outPort = mock(QuestionCommandOutPort.class);
        Question newQuestion = question("q_new");
        when(outPort.findQuestionById("q_new")).thenReturn(Optional.of(newQuestion));

        Questionnaire existing = questionnaire(List.of());
        UpdateQuestionnaireCommand command = new UpdateQuestionnaireCommand(
                "q_001",
                "APP",
                "JOURNEY_01",
                null,
                null,
                List.of(new UpdateConfiguredQuestionParam(
                        "q_new",
                        new ConfiguredQuestionParam(
                                1,
                                new AnswerConfigParam.Text(null, null),
                                new ConditionParam.Equal("q_missing", "yes")
                        )
                )),
                null,
                null,
                LocalDateTime.parse("2026-01-02T10:00:00")
        );

        ResolveConfiguredQuestionsStep step = new ResolveConfiguredQuestionsStep(outPort);
        UpdateQuestionnairePipelineContext context = new UpdateQuestionnairePipelineContext(command);
        context.existingQuestionnaire(existing);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("CONDITION_QUESTION_NOT_FOUND")));
    }

    private static AuditInfo auditInfo() {
        return OrderQuestionnaireAuditFactory.createNew(
                Id.withId("00000000-0000-0000-0000-000000000001"),
                "REF-1",
                "User",
                "u@acme.com",
                LocalDateTime.parse("2026-01-01T10:00:00")
        ).getOrElseThrow(e -> new IllegalStateException("Invalid audit: " + e));
    }

    private static Question question(String id) {
        return Question.rehydrate(id, "Label " + id, ParameterizationStatus.ACTIVE, "SKU-1", auditInfo());
    }

    private static ConfiguredQuestion configuredQuestion(String questionId) {
        return ConfiguredQuestion.createNew(question(questionId), AnswerConfigurationFactory.createTextStrategy(), 1);
    }

    private static Questionnaire questionnaire(List<ConfiguredQuestion> configuredQuestions) {
        return Questionnaire.rehydrate(
                "q_001",
                "APP",
                "JOURNEY_01",
                "desc",
                ParameterizationStatus.DRAFT,
                configuredQuestions,
                auditInfo()
        );
    }
}

