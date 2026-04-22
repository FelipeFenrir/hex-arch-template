package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionComposer;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.CompositeCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionConditionComposer;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.orderquestionnaire.testutils.mocks.audit.AuditTestData;
import com.acme.orderquestionnaire.testutils.mocks.question.QuestionMock;
import com.acme.orderquestionnaire.testutils.mocks.questionnaire.QuestionnaireMock;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Questionnaire additional coverage")
class QuestionnaireCoverageAdditionalTest {
    public static final Question QUESTION_1 = QuestionMock.DEFAULT_QUESTION_1.getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));
    public static final Question QUESTION_2 = QuestionMock.DEFAULT_QUESTION_2.getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

    @Test
    @DisplayName("Questionnaire should support entity operations and successful validation")
    void shouldCoverQuestionnaireEntityOperations() {
        ConfiguredQuestion first = requireConfigured(requireBuilder(ConfiguredQuestionFactory.from(QUESTION_1))
                .withOrder(1)
                .asNumber());
        ConfiguredQuestion second = requireConfigured(requireBuilder(ConfiguredQuestionFactory.from(QUESTION_2))
                .withOrder(2)
                .asText());
        ConfiguredQuestion removable = requireConfigured(requireBuilder(ConfiguredQuestionFactory.from(
                requireQuestion(requireQuestionBuilder(
                        QuestionFactory.createNew("removable", "Removable", "SALE", AuditTestData.createdAudit())
                ).build())
        )).withOrder(3).asText());

        Questionnaire questionnaire = QuestionnaireMock.active();
        assertTrue(questionnaire.isActive());
        assertEquals(QuestionnaireMock.DEFAULT_QUESTIONNAIRE_ID, questionnaire.questionnaireId().id());

        questionnaire.addQuestion(first);
        questionnaire.addQuestions(List.of(second));
        questionnaire.addQuestions(removable);
        questionnaire.removeQuestion("removable");
        questionnaire.removeQuestion("missing");

        assertEquals(List.of("how_satisfied_are_you", "you_recommend_us"),
                questionnaire.getOrderedQuestions().stream().map(Question::id).toList());

        Result<Void, List<QuestionValidationFailure>> validation = questionnaire.answerValidation(Map.of(
                QUESTION_1.id(), 7,
                QUESTION_2.id(), "valid text"
        ));
        assertInstanceOf(Result.Success.class, validation);

        assertNotNull(questionnaire.toTree());
        assertEquals(2, questionnaire.toTree().questions().size());

        Questionnaire rehydratedWithList = Questionnaire.rehydrate(
                QuestionnaireId.of("survey_2", "channel", "journey"),
                "description", ParameterizationStatus.DRAFT, List.of(first, second), AuditTestData.createdAudit());
        assertFalse(rehydratedWithList.isActive());
        assertEquals(2, rehydratedWithList.getOrderedQuestions().size());
    }

    @Test
    @DisplayName("QuestionnaireFactory should cover success overloads and helper methods")
    void shouldCoverQuestionnaireFactorySuccessPathsAndHelpers() {
        ConfiguredQuestion configuredFromResult = requireConfigured(requireBuilder(ConfiguredQuestionFactory
                .from(QUESTION_1)).asText());
        ConfiguredQuestion configuredFromList = requireConfigured(requireBuilder(ConfiguredQuestionFactory
                .from(QUESTION_2)).asText());
        ConfiguredQuestion configuredFromVarargs = requireConfigured(requireBuilder(ConfiguredQuestionFactory
                .from(QUESTION_1)).asNumber());

        Questionnaire questionnaire = requireQuestionnaire(requireQuestionnaireBuilder(QuestionnaireFactory
                .createNew(QuestionnaireMock.DEFAULT_QUESTIONNAIRE_ID,
                        QuestionnaireMock.DEFAULT_CHANNEL_CODE,
                        QuestionnaireMock.DEFAULT_JOURNEY_CODE,
                        QuestionnaireMock.DEFAULT_DESCRIPTION,
                        AuditTestData.createdAudit()))
                .withQuestion(Result.success(configuredFromResult))
                .withQuestion(requireConfigured(requireBuilder(ConfiguredQuestionFactory
                        .from(QUESTION_1)).asText()))
                .withQuestion(requireConfigured(requireBuilder(ConfiguredQuestionFactory
                        .from(QUESTION_2)).asText(new ConfiguredQuestionFactory.TextConfig("[A-Za-z ]+", "Text only"))))
                .withQuestions(List.of(configuredFromList))
                .withQuestions(configuredFromVarargs)
                .build());

        assertEquals(5, questionnaire.getOrderedQuestions().size());

        AnswerOptionItem option = AnswerOptionComposer.option("yes", "Yes");
        assertEquals("yes", option.value());
        assertNull(QuestionConditionComposer.combineConditions(true));
        assertNull(QuestionConditionComposer.combineConditions(false, (QuestionCondition[]) null));

        ConfiguredQuestionFactory.ListConfig options = AnswerOptionComposer.options(option);
        assertEquals(1, options.options().size());
        assertNull(options.customErrorMessage());

        ConfiguredQuestionFactory.ListConfig optionsWithMessage = AnswerOptionComposer.options("Pick one", option);
        assertEquals("Pick one", optionsWithMessage.customErrorMessage());

        ConfiguredQuestionFactory.ListConfig composedOptions = AnswerOptionComposer.compose()
                .add("maybe", "Maybe")
                .add(option)
                .withCustomErrorMessage("Choose one")
                .build();
        assertEquals(2, composedOptions.options().size());
        assertEquals("Choose one", composedOptions.customErrorMessage());

        QuestionCondition simpleCondition = QuestionConditionComposer.condition(new NumericCondition("score", 7, ">"))
                .build();
        assertTrue(simpleCondition.isSatisfy(Map.of("score", 8)));

        CompositeCondition composite = new CompositeCondition(true);
        composite.addCondition(new EqualCondition("approved", true));
        assertSame(composite, QuestionConditionComposer.composeWithAnd(composite));
        assertInstanceOf(CompositeCondition.class,
                QuestionConditionComposer.composeWithOr(new EqualCondition("approved", true), new EqualCondition("manual", true)));
        assertInstanceOf(CompositeCondition.class,
                QuestionConditionComposer.composeWithAnd(new EqualCondition("approved", true), new EqualCondition("manual", true)));
    }

    @Test
    @DisplayName("QuestionnaireFactory should accumulate builder errors and validate null conditions")
    void shouldCoverQuestionnaireFactoryErrorPaths() {
        Result<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>> invalidCreate =
                QuestionnaireFactory.createNew(null, null, null, null, null);
        Result<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>> invalidCreateWithBlankJourney =
                QuestionnaireFactory.createNew("survey", "channel", " ", "desc", AuditTestData.createdAudit());
        assertInstanceOf(Result.Failure.class, invalidCreate);
        assertEquals(5, invalidCreate.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure")).size());
        assertInstanceOf(Result.Failure.class, invalidCreateWithBlankJourney);
        assertEquals(List.of(QuestionnaireDomainErrors.requiredField("journeyDistributionId")),
                invalidCreateWithBlankJourney.errorOrElseThrow(() ->
                        new IllegalStateException("Expected failure")));

        Result<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>> invalidRehydrateWithNulls =
                QuestionnaireFactory.rehydrate(null, null, null, null, ParameterizationStatus.ACTIVE, null);
        Result<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>> invalidRehydrateWithBlankChannel =
                QuestionnaireFactory.rehydrate("survey", " ", "journey", "desc", ParameterizationStatus.ACTIVE, AuditTestData.createdAudit());
        assertInstanceOf(Result.Failure.class, invalidRehydrateWithNulls);
        assertEquals(5, invalidRehydrateWithNulls.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure")).size());
        assertInstanceOf(Result.Failure.class, invalidRehydrateWithBlankChannel);
        assertEquals(List.of(QuestionnaireDomainErrors.requiredField("channelDistributionId")),
                invalidRehydrateWithBlankChannel.errorOrElseThrow(() ->
                        new IllegalStateException("Expected failure")));

        Result<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>> invalidRehydrate =
                QuestionnaireFactory.rehydrate(" ", null, "", " ", null, null);
        assertInstanceOf(Result.Failure.class, invalidRehydrate);
        assertEquals(6, invalidRehydrate.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure")).size());

        Result<Questionnaire, List<DomainError>> buildFailure = requireQuestionnaireBuilder(QuestionnaireFactory
                .createNew("survey_id", "channel", "journey", "desc", AuditTestData.createdAudit()))
                .withQuestion((ConfiguredQuestion) null)
                .withQuestion((Result<ConfiguredQuestion, List<DomainError>>) null)
                .withQuestion(Result.failure(List.of(new DomainError("ERR", "broken"))))
                .withQuestions((List<ConfiguredQuestion>) null)
                .withQuestions((ConfiguredQuestion[]) null)
                .build();

        assertInstanceOf(Result.Failure.class, buildFailure);
        List<DomainError> errors = buildFailure.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(List.of(
                QuestionnaireDomainErrors.requiredObject("question"),
                QuestionnaireDomainErrors.requiredObject("questionResult"),
                new DomainError("ERR", "broken"),
                QuestionnaireDomainErrors.requiredObject("questionsList"),
                QuestionnaireDomainErrors.requiredObject("questionArray")
        ), errors);

        Result<Questionnaire, List<DomainError>> rehydratedBuilderFailure = requireQuestionnaireBuilder(
                QuestionnaireFactory.rehydrate("survey_id", "channel", "journey", "desc", ParameterizationStatus.ACTIVE, AuditTestData.createdAudit())
        ).withQuestion((ConfiguredQuestion) null).build();
        assertInstanceOf(Result.Failure.class, rehydratedBuilderFailure);
        assertEquals(List.of(QuestionnaireDomainErrors.requiredObject("question")),
                rehydratedBuilderFailure.errorOrElseThrow(() ->
                        new IllegalStateException("Expected failure")));

        try (MockedStatic<ConfiguredQuestionFactory> mockedStatic = Mockito.mockStatic(ConfiguredQuestionFactory.class)) {
            mockedStatic.when(() -> ConfiguredQuestionFactory.from(QUESTION_1))
                    .thenReturn(Result.failure(List.of(new DomainError("MOCKED", "builder failure"))));

            Result<Questionnaire, List<DomainError>> mockedFailure = requireQuestionnaireBuilder(QuestionnaireFactory
                    .createNew("survey_mock", "channel", "journey", "desc", AuditTestData.createdAudit()))
                    .withQuestion(ConfiguredQuestionFactory.from(QUESTION_1)
                            .flatMap(builder -> builder.asText(new ConfiguredQuestionFactory.TextConfig(null, null))))
                    .build();

            assertInstanceOf(Result.Failure.class, mockedFailure);
            assertEquals(List.of(new DomainError("MOCKED", "builder failure")),
                    mockedFailure.errorOrElseThrow(() ->
                            new IllegalStateException("Expected failure")));
        }

        assertThrows(NullPointerException.class, () -> QuestionConditionComposer.condition(null));
        QuestionConditionComposer.Composer composer = QuestionConditionComposer.condition(new EqualCondition("x", 1));
        assertThrows(NullPointerException.class, () -> composer.and(null));
        assertThrows(NullPointerException.class, () -> composer.or(null));
    }

    private static QuestionnaireFactory.QuestionnaireBuilder requireQuestionnaireBuilder(
            Result<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>> result
    ) {
        return result.getOrElseThrow(error ->
                new IllegalStateException("Expected success questionnaire builder but got failure: " + error));
    }

    private static ConfiguredQuestionFactory.ConfiguredQuestionBuilder requireBuilder(
            Result<ConfiguredQuestionFactory.ConfiguredQuestionBuilder, List<DomainError>> result
    ) {
        return result.getOrElseThrow(error ->
                new IllegalStateException("Expected success configured builder but got failure: " + error));
    }

    private static QuestionFactory.NewQuestionBuilder requireQuestionBuilder(
            Result<QuestionFactory.NewQuestionBuilder, List<DomainError>> result
    ) {
        return result.getOrElseThrow(error ->
                new IllegalStateException("Expected success question builder but got failure: " + error));
    }

    private static Question requireQuestion(Result<Question, List<DomainError>> result) {
        return result.getOrElseThrow(error ->
                new IllegalStateException("Expected success question but got failure: " + error));
    }

    private static ConfiguredQuestion requireConfigured(Result<ConfiguredQuestion, List<DomainError>> result) {
        return result.getOrElseThrow(error ->
                new IllegalStateException("Expected success configured question but got failure: " + error));
    }

    private static Questionnaire requireQuestionnaire(Result<Questionnaire, List<DomainError>> result) {
        return result.getOrElseThrow(error ->
                new IllegalStateException("Expected success questionnaire but got failure: " + error));
    }

}



