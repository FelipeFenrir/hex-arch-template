package com.acme.orderquestionnaire.domain;

import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditInfo;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditUser;
import com.acme.orderquestionnaire.domain.audit.validation.AuditInfoValidationRules;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.question.QuestionStatusMachine;
import com.acme.orderquestionnaire.domain.question.QuestionStatusTransitionContext;
import com.acme.orderquestionnaire.domain.question.errors.QuestionDomainErrors;
import com.acme.orderquestionnaire.domain.question.validation.QuestionBuilderRules;
import com.acme.orderquestionnaire.domain.question.validation.QuestionCreationRules;
import com.acme.orderquestionnaire.domain.question.validation.QuestionRehydrationRules;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionValidationFailure;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireStatusMachine;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireStatusTransitionContext;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionComposer;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionConditionComposer;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.VisibilityCondition;
import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.orderquestionnaire.domain.questionnaire.validation.QuestionnaireBuilderRules;
import com.acme.orderquestionnaire.domain.questionnaire.validation.QuestionnaireIdentityRules;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.orderquestionnaire.testutils.mocks.audit.AuditTestData;
import com.acme.shared.engine.state.TransitionResult;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.AuditUser;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Domain coverage completion")
class DomainCoverageCompletionTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.parse("2026-01-01T10:00:00");
    private static final LocalDateTime UPDATED_AT = LocalDateTime.parse("2026-01-02T11:00:00");
    private static final Id USER_ID = Id.withId("11111111-1111-1111-1111-111111111111");
    private static final Id UPDATED_USER_ID = Id.withId("22222222-2222-2222-2222-222222222222");

    @Test
    @DisplayName("should cover utility constructors, domain error factories and validation rule factories")
    void shouldCoverUtilityConstructorsAndRuleFactories() throws Exception {
        assertUtilityClass(OrderQuestionnaireAuditFactory.class);
        assertUtilityClass(AuditInfoValidationRules.class);
        assertUtilityClass(QuestionFactory.class);
        assertUtilityClass(QuestionStatusMachine.class);
        assertUtilityClass(QuestionBuilderRules.class);
        assertUtilityClass(QuestionCreationRules.class);
        assertUtilityClass(QuestionRehydrationRules.class);
        assertUtilityClass(QuestionDomainErrors.class);
        assertUtilityClass(QuestionnaireFactory.class);
        assertUtilityClass(AnswerOptionComposer.class);
        assertUtilityClass(QuestionConditionComposer.class);
        assertUtilityClass(QuestionnaireBuilderRules.class);
        assertUtilityClass(QuestionnaireIdentityRules.class);
        assertUtilityClass(QuestionnaireDomainErrors.class);

        assertEquals(new DomainError("INVALID_STATUS_TRANSITION", "Cannot transition from DRAFT to ACTIVE"),
                QuestionDomainErrors.invalidStatusTransition(ParameterizationStatus.DRAFT, ParameterizationStatus.ACTIVE));
        assertEquals(new DomainError("INVALID_CREATED_AT", "createdAt must not be null"),
                QuestionDomainErrors.invalidCreatedAt());
        assertEquals(new DomainError("INVALID_UPDATED_AT", "updatedAt must not be null"),
                QuestionDomainErrors.invalidUpdatedAt());

        assertEquals(new DomainError("INVALID_STATUS_TRANSITION", "Cannot transition from DRAFT to ACTIVE"),
                QuestionnaireDomainErrors.invalidStatusTransition(ParameterizationStatus.DRAFT, ParameterizationStatus.ACTIVE));
        assertEquals(new DomainError("QUESTIONNAIRE_UPDATE_NOT_ALLOWED",
                        "questionnaire can only be structurally updated while in DRAFT or INACTIVE"),
                QuestionnaireDomainErrors.questionnaireUpdateNotAllowed());
        assertEquals(new DomainError("INVALID_UPDATED_AT", "updatedAt must not be null"),
                QuestionnaireDomainErrors.invalidUpdatedAt());

        assertTrue(AuditInfoValidationRules.userIdForQuestionnaire().isSatisfiedBy(USER_ID));
        assertFalse(AuditInfoValidationRules.userIdForQuestionnaire().isSatisfiedBy(null));
        assertEquals(QuestionnaireDomainErrors.invalidUserId(),
                AuditInfoValidationRules.userIdForQuestionnaire().toDomainError());

        assertTrue(AuditInfoValidationRules.userNameForQuestionnaire().isSatisfiedBy("User"));
        assertFalse(AuditInfoValidationRules.userNameForQuestionnaire().isSatisfiedBy(null));
        assertFalse(AuditInfoValidationRules.userNameForQuestionnaire().isSatisfiedBy(" "));
        assertEquals(QuestionnaireDomainErrors.invalidUserName(),
                AuditInfoValidationRules.userNameForQuestionnaire().toDomainError());

        assertTrue(AuditInfoValidationRules.createdAtForQuestionnaire().isSatisfiedBy(CREATED_AT));
        assertFalse(AuditInfoValidationRules.updatedAtForQuestionnaire().isSatisfiedBy(null));
        assertEquals(QuestionnaireDomainErrors.invalidUpdatedAt(),
                AuditInfoValidationRules.updatedAtForQuestionnaire().toDomainError());

        assertTrue(AuditInfoValidationRules.userIdForQuestion().isSatisfiedBy(USER_ID));
        assertFalse(AuditInfoValidationRules.userIdForQuestion().isSatisfiedBy(null));
        assertEquals(QuestionDomainErrors.invalidUserId(),
                AuditInfoValidationRules.userIdForQuestion().toDomainError());

        assertTrue(AuditInfoValidationRules.userNameForQuestion().isSatisfiedBy("Question User"));
        assertFalse(AuditInfoValidationRules.userNameForQuestion().isSatisfiedBy(null));
        assertFalse(AuditInfoValidationRules.userNameForQuestion().isSatisfiedBy(""));
        assertEquals(QuestionDomainErrors.invalidUserName(),
                AuditInfoValidationRules.userNameForQuestion().toDomainError());

        assertTrue(AuditInfoValidationRules.createdAtForQuestion().isSatisfiedBy(CREATED_AT));
        assertFalse(AuditInfoValidationRules.createdAtForQuestion().isSatisfiedBy(null));
        assertEquals(QuestionDomainErrors.invalidCreatedAt(),
                AuditInfoValidationRules.createdAtForQuestion().toDomainError());

        assertTrue(AuditInfoValidationRules.updatedAtForQuestion().isSatisfiedBy(UPDATED_AT));
        assertFalse(AuditInfoValidationRules.updatedAtForQuestion().isSatisfiedBy(null));
        assertEquals(QuestionDomainErrors.invalidUpdatedAt(),
                AuditInfoValidationRules.updatedAtForQuestion().toDomainError());

        assertEquals(1, AuditInfoValidationRules.questionnaireUpdatedAtRules().size());
        assertEquals(1, AuditInfoValidationRules.questionCreatedAtRules().size());

        assertFalse(QuestionBuilderRules.salesItemReferenceCodeNotNull().isSatisfiedBy(" "));
        assertEquals(QuestionDomainErrors.requiredField("salesItemReferenceCode"),
                QuestionBuilderRules.salesItemReferenceCodeNotNull().toDomainError());
        assertTrue(QuestionBuilderRules.auditInfoNotNull().isSatisfiedBy(audit()));
        assertFalse(QuestionBuilderRules.auditInfoNotNull().isSatisfiedBy(null));
        assertEquals(QuestionDomainErrors.requiredObject("auditInfo"),
                QuestionBuilderRules.auditInfoNotNull().toDomainError());
        assertEquals(1, QuestionBuilderRules.auditInfoRules().size());

        assertFalse(QuestionCreationRules.questionIdNotNull().isSatisfiedBy(" "));
        assertFalse(QuestionCreationRules.questionIdSnakeCaseFormat().isSatisfiedBy("InvalidId"));
        assertEquals(QuestionDomainErrors.invalidIdFormat(),
                QuestionCreationRules.questionIdSnakeCaseFormat().toDomainError());
        assertFalse(QuestionCreationRules.questionLabelNotNull().isSatisfiedBy(null));
        assertFalse(QuestionCreationRules.auditInfoNotNull().isSatisfiedBy(null));

        assertFalse(QuestionRehydrationRules.questionIdNotNull().isSatisfiedBy(null));
        assertFalse(QuestionRehydrationRules.questionLabelNotNull().isSatisfiedBy(" "));
        assertFalse(QuestionRehydrationRules.questionStatusNotNull().isSatisfiedBy(null));
        assertEquals(QuestionDomainErrors.requiredObject("status"),
                QuestionRehydrationRules.questionStatusNotNull().toDomainError());
        assertFalse(QuestionRehydrationRules.auditInfoNotNull().isSatisfiedBy(null));

        assertTrue(QuestionnaireBuilderRules.orderNotNegative().isSatisfiedBy(null));
        assertTrue(QuestionnaireBuilderRules.orderNotNegative().isSatisfiedBy(0));
        assertFalse(QuestionnaireBuilderRules.orderNotNegative().isSatisfiedBy(-1));
        assertEquals(QuestionnaireDomainErrors.invalidOrder(),
                QuestionnaireBuilderRules.orderNotNegative().toDomainError());
        assertEquals(1, QuestionnaireBuilderRules.orderValidationRules().size());

        assertFalse(QuestionnaireIdentityRules.questionnaireIdNotNull().isSatisfiedBy(" "));
        assertFalse(QuestionnaireIdentityRules.questionnaireIdSnakeCaseFormat().isSatisfiedBy("InvalidId"));
        assertFalse(QuestionnaireIdentityRules.questionnaireChannelNotNull().isSatisfiedBy(null));
        assertFalse(QuestionnaireIdentityRules.questionnaireJourneyNotNull().isSatisfiedBy(" "));
        assertFalse(QuestionnaireIdentityRules.questionnaireDescriptionNotNull().isSatisfiedBy(null));
        assertEquals(QuestionnaireDomainErrors.requiredField("description"),
                QuestionnaireIdentityRules.questionnaireDescriptionNotNull().toDomainError());
    }

    @Test
    @DisplayName("should cover audit factory variants and audit record defensive branches")
    void shouldCoverAuditFactoryVariantsAndAuditRecords() {
        Result<AuditUser, List<DomainError>> questionUserResult = OrderQuestionnaireAuditFactory.userForQuestion(
                USER_ID,
                "REF-QUESTION",
                "Question User",
                "question@acme.com"
        );
        assertInstanceOf(Result.Success.class, questionUserResult);
        AuditUser questionUser = questionUserResult.getOrElseThrow(error -> new IllegalStateException("Expected success: " + error));
        assertEquals("Question User", questionUser.name());

        Result<AuditInfo, List<DomainError>> questionAuditResult = OrderQuestionnaireAuditFactory.createNewForQuestion(
                USER_ID,
                "REF-QUESTION",
                "Question User",
                "question@acme.com",
                CREATED_AT
        );
        assertInstanceOf(Result.Success.class, questionAuditResult);
        AuditInfo questionAudit = questionAuditResult.getOrElseThrow(error -> new IllegalStateException("Expected success: " + error));
        assertEquals(CREATED_AT, questionAudit.createdAt());

        Result<AuditUser, List<DomainError>> updateQuestionnaireSuccess = OrderQuestionnaireAuditFactory.updateUserForQuestionnaire(
                USER_ID,
                "REF-UPD",
                "Updated Questionnaire User",
                "updated@acme.com",
                UPDATED_AT
        );
        assertInstanceOf(Result.Success.class, updateQuestionnaireSuccess);
        assertEquals("Updated Questionnaire User",
                updateQuestionnaireSuccess.getOrElseThrow(error -> new IllegalStateException("Expected success: " + error)).name());

        Result<AuditUser, List<DomainError>> updateQuestionSuccess = OrderQuestionnaireAuditFactory.updateUserForQuestion(
                USER_ID,
                "REF-UPD",
                "Updated Question User",
                "updated-question@acme.com",
                UPDATED_AT
        );
        assertInstanceOf(Result.Success.class, updateQuestionSuccess);
        assertEquals("Updated Question User",
                updateQuestionSuccess.getOrElseThrow(error -> new IllegalStateException("Expected success: " + error)).name());

        Result<AuditUser, List<DomainError>> updateQuestionFailure = OrderQuestionnaireAuditFactory.updateUserForQuestion(
                null,
                "REF-UPD",
                " ",
                "updated-question@acme.com",
                UPDATED_AT
        );
        assertInstanceOf(Result.Failure.class, updateQuestionFailure);
        List<DomainError> updateQuestionErrors = updateQuestionFailure.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(updateQuestionErrors.stream().anyMatch(error -> error.code().equals("INVALID_USER_ID")));
        assertTrue(updateQuestionErrors.stream().anyMatch(error -> error.code().equals("INVALID_USER_NAME")));

        Result<AuditUser, List<DomainError>> updateQuestionnaireFailure = OrderQuestionnaireAuditFactory.updateUserForQuestionnaire(
                null,
                "REF-UPD",
                " ",
                "updated@acme.com",
                null
        );
        assertInstanceOf(Result.Failure.class, updateQuestionnaireFailure);
        List<DomainError> failureErrors = updateQuestionnaireFailure.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(failureErrors.stream().anyMatch(error -> error.code().equals("INVALID_UPDATED_AT")));
        assertTrue(failureErrors.stream().anyMatch(error -> error.code().equals("INVALID_USER_ID")));
        assertTrue(failureErrors.stream().anyMatch(error -> error.code().equals("INVALID_USER_NAME")));

        OrderQuestionnaireAuditUser createdBy = new OrderQuestionnaireAuditUser(USER_ID, "REF", "Creator", "creator@acme.com");
        assertThrows(IllegalArgumentException.class, () -> new OrderQuestionnaireAuditUser(null, "REF", "Creator", "creator@acme.com"));
        assertThrows(IllegalArgumentException.class, () -> new OrderQuestionnaireAuditUser(USER_ID, "REF", null, "creator@acme.com"));
        assertThrows(IllegalArgumentException.class, () -> new OrderQuestionnaireAuditUser(USER_ID, "REF", " ", "creator@acme.com"));

        assertThrows(IllegalArgumentException.class, () -> new OrderQuestionnaireAuditInfo(null, CREATED_AT, null, null));
        assertThrows(IllegalArgumentException.class, () -> new OrderQuestionnaireAuditInfo(createdBy, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new OrderQuestionnaireAuditInfo(createdBy, CREATED_AT, createdBy, null));

        OrderQuestionnaireAuditInfo auditInfo = new OrderQuestionnaireAuditInfo(createdBy, CREATED_AT, null, null);
        AuditInfo updatedAudit = auditInfo.withUpdate(new OrderQuestionnaireAuditUser(UPDATED_USER_ID, "REF2", "Updater", "updater@acme.com"), UPDATED_AT);
        assertEquals(UPDATED_AT, updatedAudit.updatedAt());
        assertEquals("Updater", updatedAudit.updatedBy().name());
    }

    @Test
    @DisplayName("should cover question validation helpers, builder internals and status transitions")
    void shouldCoverQuestionHelpersBuildersAndStatusTransitions() throws Exception {
        AuditInfo audit = AuditTestData.createdAudit();
        Question question = Question.createNew("question_code", "Question label", "SALE", audit);

        assertInstanceOf(Result.Success.class, QuestionFactory.validateQuestionId("valid_question"));
        Result<Void, List<DomainError>> invalidQuestionId = QuestionFactory.validateQuestionId("InvalidQuestion");
        assertInstanceOf(Result.Failure.class, invalidQuestionId);
        assertEquals(List.of(QuestionDomainErrors.invalidIdFormat()),
                invalidQuestionId.errorOrElseThrow(() -> new IllegalStateException("Expected failure")));

        Result<Void, List<DomainError>> invalidCreatePayload = QuestionFactory.validateCreatePayload(null, " ", " ");
        assertInstanceOf(Result.Failure.class, invalidCreatePayload);
        assertEquals(3, invalidCreatePayload.errorOrElseThrow(() -> new IllegalStateException("Expected failure")).size());
        assertInstanceOf(Result.Success.class, QuestionFactory.validateCreatePayload("valid_question", "Label", "SALE"));

        Result<Void, List<DomainError>> invalidUpdatePayload = QuestionFactory.validateUpdatePayload(" ", null, null);
        assertInstanceOf(Result.Failure.class, invalidUpdatePayload);
        assertEquals(3, invalidUpdatePayload.errorOrElseThrow(() -> new IllegalStateException("Expected failure")).size());
        assertInstanceOf(Result.Success.class, QuestionFactory.validateUpdatePayload("valid_question", "Label", "SALE"));

        assertInstanceOf(Result.Success.class,
                QuestionFactory.createNewBuilder()
                        .withId("manual_question")
                        .withLabel("Manual question")
                        .withSalesItemReferenceCode("SALE")
                        .withAuditInfo(audit)
                        .build());

        assertInstanceOf(Result.Success.class,
                QuestionFactory.rehydratedBuilder()
                        .withId("manual_question")
                        .withLabel("Manual question")
                        .withStatus(ParameterizationStatus.ACTIVE)
                        .withSalesItemReferenceCode("SALE")
                        .withAuditInfo(audit)
                        .build());

        QuestionFactory.NewQuestionBuilder newBuilder = QuestionFactory.createNew("valid_question", "Label", "SALE", audit)
                .getOrElseThrow(error -> new IllegalStateException("Expected builder success: " + error));
        QuestionFactory.RehydratedQuestionBuilder rehydratedBuilder = QuestionFactory.rehydrate(
                        "valid_question",
                        "Label",
                        ParameterizationStatus.ACTIVE,
                        "SALE",
                        audit)
                .getOrElseThrow(error -> new IllegalStateException("Expected builder success: " + error));

        assertSame(newBuilder, newBuilder
                .withId("valid_question")
                .withLabel("Label")
                .withSalesItemReferenceCode("SALE")
                .withAuditInfo(audit));

        assertSame(rehydratedBuilder, rehydratedBuilder
                .withId("valid_question")
                .withLabel("Label")
                .withStatus(ParameterizationStatus.ACTIVE)
                .withSalesItemReferenceCode("SALE")
                .withAuditInfo(audit));

        injectBuilderError(newBuilder, new DomainError("ERR", "forced"));
        injectBuilderError(rehydratedBuilder, new DomainError("ERR", "forced"));

        Result<Question, List<DomainError>> newBuildFailure = newBuilder.build();
        assertInstanceOf(Result.Failure.class, newBuildFailure);
        assertEquals(List.of(new DomainError("ERR", "forced")),
                newBuildFailure.errorOrElseThrow(() -> new IllegalStateException("Expected failure")));

        Result<Question, List<DomainError>> rehydratedBuildFailure = rehydratedBuilder.build();
        assertInstanceOf(Result.Failure.class, rehydratedBuildFailure);
        assertEquals(List.of(new DomainError("ERR", "forced")),
                rehydratedBuildFailure.errorOrElseThrow(() -> new IllegalStateException("Expected failure")));

        assertSame(audit, question.auditInfo());
        assertFalse(question.isActive());

        AuditUser actor = OrderQuestionnaireAuditFactory.userForQuestion(USER_ID, "REF", "Actor", "actor@acme.com")
                .getOrElseThrow(error -> new IllegalStateException("Expected actor success: " + error));
        QuestionStatusTransitionContext baseContext = new QuestionStatusTransitionContext(question, actor, UPDATED_AT, audit, false);
        QuestionStatusTransitionContext changedAudit = baseContext.withAuditInfo(audit.withUpdate(actor, UPDATED_AT));
        QuestionStatusTransitionContext changedStatus = baseContext.withStatusChanged(true);
        assertEquals(UPDATED_AT, changedAudit.auditInfo().updatedAt());
        assertTrue(changedStatus.statusChanged());

        Result<ParameterizationStatus, List<DomainError>> validateDraftToActive =
                QuestionStatusMachine.validateTransition(ParameterizationStatus.DRAFT, ParameterizationStatus.ACTIVE, baseContext);
        assertInstanceOf(Result.Success.class, validateDraftToActive);
        assertEquals(ParameterizationStatus.ACTIVE,
                validateDraftToActive.getOrElseThrow(error -> new IllegalStateException("Expected success: " + error)));

        Result<TransitionResult<ParameterizationStatus, QuestionStatusTransitionContext>, List<DomainError>> inactiveToActive =
                QuestionStatusMachine.transition(ParameterizationStatus.INACTIVE, ParameterizationStatus.ACTIVE, baseContext);
        assertInstanceOf(Result.Success.class, inactiveToActive);
        assertTrue(inactiveToActive.getOrElseThrow(error -> new IllegalStateException("Expected success: " + error))
                .context().statusChanged());

        Result<TransitionResult<ParameterizationStatus, QuestionStatusTransitionContext>, List<DomainError>> activeToInactive =
                QuestionStatusMachine.transition(ParameterizationStatus.ACTIVE, ParameterizationStatus.INACTIVE, baseContext);
        assertInstanceOf(Result.Success.class, activeToInactive);
        assertEquals(ParameterizationStatus.INACTIVE,
                activeToInactive.getOrElseThrow(error -> new IllegalStateException("Expected success: " + error)).targetState());

        QuestionStatusTransitionContext missingBoth = new QuestionStatusTransitionContext(question, null, null, audit, false);
        Result<TransitionResult<ParameterizationStatus, QuestionStatusTransitionContext>, List<DomainError>> missingMetadata =
                QuestionStatusMachine.transition(ParameterizationStatus.DRAFT, ParameterizationStatus.ACTIVE, missingBoth);
        assertInstanceOf(Result.Failure.class, missingMetadata);

        QuestionStatusTransitionContext missingOccurredAt = new QuestionStatusTransitionContext(question, actor, null, audit, false);
        Result<TransitionResult<ParameterizationStatus, QuestionStatusTransitionContext>, List<DomainError>> missingOccurredAtResult =
                QuestionStatusMachine.transition(ParameterizationStatus.DRAFT, ParameterizationStatus.ACTIVE, missingOccurredAt);
        assertInstanceOf(Result.Failure.class, missingOccurredAtResult);
        assertTrue(missingOccurredAtResult.errorOrElseThrow(() -> new IllegalStateException("Expected failure"))
                .stream().anyMatch(error -> error.code().equals("TRANSITION_GUARD_VIOLATION")));
    }

    @Test
    @DisplayName("should cover questionnaire entity helpers, duplicate status merge and hidden answer branch")
    void shouldCoverQuestionnaireEntityHelpersAndBranches() {
        AuditInfo audit = AuditTestData.createdAudit();
        Question question = Question.rehydrate("question_one", "Question one", ParameterizationStatus.ACTIVE, "SALE", audit);
        ConfiguredQuestion visibleConfigured = ConfiguredQuestion.createNew(question, AnswerConfigurationFactory.createTextStrategy(), 1);
        ConfiguredQuestion hiddenConfigured = ConfiguredQuestion.createNew(question, AnswerConfigurationFactory.createTextStrategy(), 2);
        hiddenConfigured.rootCondition(new VisibilityCondition("root_question", "YES"));

        Result<Void, QuestionValidationFailure> hiddenValidation = hiddenConfigured.validate(Map.of(
                "root_question", "NO",
                question.id(), "unexpected"
        ));
        assertInstanceOf(Result.Failure.class, hiddenValidation);
        assertTrue(hiddenValidation.errorOrElseThrow(() -> new IllegalStateException("Expected failure")).errors().stream()
                .anyMatch(error -> error.code().equals("ANSWER_NOT_ALLOWED_BY_CONDITION")));

        Result<Void, QuestionValidationFailure> visibleValidation = hiddenConfigured.validate(Map.of(
                "root_question", "YES",
                question.id(), "expected"
        ));
        assertInstanceOf(Result.Success.class, visibleValidation);

        Result<Void, QuestionValidationFailure> visibleValidationWithMissingReferencedStatus = hiddenConfigured.validate(
                Map.of(
                        "root_question", "YES",
                        question.id(), "expected"
                ),
                Map.of("unrelated_question", ParameterizationStatus.ACTIVE)
        );
        assertInstanceOf(Result.Success.class, visibleValidationWithMissingReferencedStatus);

        VisibilityCondition visibilityCondition = new VisibilityCondition("root_question", "YES");
        assertEquals(Set.of("root_question"), visibilityCondition.referencedQuestionIds());

        Questionnaire createdFromStrings = Questionnaire.createNew("survey_id", "channel", "journey", "Description", audit);
        assertTrue(createdFromStrings.canBeDeleted());
        assertEquals("survey_id", createdFromStrings.id());

        Questionnaire rehydratedWithoutQuestions = Questionnaire.rehydrate(
                QuestionnaireId.of("survey_id", "channel", "journey"),
                "Description",
                ParameterizationStatus.INACTIVE,
                audit
        );
        assertTrue(rehydratedWithoutQuestions.canBeDeleted());
        assertFalse(rehydratedWithoutQuestions.isActive());

        Questionnaire rehydratedWithStringOverload = Questionnaire.rehydrate(
                "survey_id",
                "channel",
                "journey",
                "Description",
                ParameterizationStatus.ACTIVE,
                audit
        );
        assertFalse(rehydratedWithStringOverload.canBeDeleted());
        assertTrue(rehydratedWithStringOverload.isActive());

        Questionnaire rehydratedWithNullList = Questionnaire.rehydrate(
                QuestionnaireId.of("survey_null", "channel", "journey"),
                "Description",
                ParameterizationStatus.DRAFT,
                null,
                audit
        );
        assertNotNull(rehydratedWithNullList.configuredQuestions());
        assertTrue(rehydratedWithNullList.configuredQuestions().isEmpty());

        Questionnaire notReadyBecauseNullQuestion = Questionnaire.rehydrate(
                QuestionnaireId.of("survey_null_question", "channel", "journey"),
                "Description",
                ParameterizationStatus.DRAFT,
                Collections.singletonList(null),
                audit
        );
        assertFalse(notReadyBecauseNullQuestion.isReadyToActivate());

        ConfiguredQuestion noAnswerConfiguration = ConfiguredQuestion.createNew(question, null, 0);
        Questionnaire notReadyBecauseMissingAnswerConfig = Questionnaire.rehydrate(
                QuestionnaireId.of("survey_missing_answer", "channel", "journey"),
                "Description",
                ParameterizationStatus.DRAFT,
                List.of(noAnswerConfiguration),
                audit
        );
        assertFalse(notReadyBecauseMissingAnswerConfig.isReadyToActivate());

        ConfiguredQuestion negativeOrder = ConfiguredQuestion.createNew(question, AnswerConfigurationFactory.createTextStrategy(), -1);
        Questionnaire notReadyBecauseNegativeOrder = Questionnaire.rehydrate(
                QuestionnaireId.of("survey_negative_order", "channel", "journey"),
                "Description",
                ParameterizationStatus.DRAFT,
                List.of(negativeOrder),
                audit
        );
        assertFalse(notReadyBecauseNegativeOrder.isReadyToActivate());

        Questionnaire readyQuestionnaire = Questionnaire.rehydrate(
                QuestionnaireId.of("survey_ready", "channel", "journey"),
                "Description",
                ParameterizationStatus.DRAFT,
                List.of(visibleConfigured),
                audit
        );
        assertTrue(readyQuestionnaire.isReadyToActivate());

        Questionnaire duplicatedQuestionnaire = Questionnaire.rehydrate(
                QuestionnaireId.of("survey_duplicates", "channel", "journey"),
                "Description",
                ParameterizationStatus.ACTIVE,
                List.of(visibleConfigured, ConfiguredQuestion.createNew(question, AnswerConfigurationFactory.createTextStrategy(), 3)),
                audit
        );
        Result<Void, List<QuestionValidationFailure>> duplicateValidation = duplicatedQuestionnaire.answerValidation(Map.of(question.id(), "value"));
        assertInstanceOf(Result.Success.class, duplicateValidation);

        ConfiguredQuestion configuredFromNullTextConfig = ConfiguredQuestionFactory.from(question)
                .getOrElseThrow(error -> new IllegalStateException("Expected builder success: " + error))
                .asText(null)
                .getOrElseThrow(error -> new IllegalStateException("Expected configured question success: " + error));
        assertEquals(question.id(), configuredFromNullTextConfig.question().id());
    }

    @Test
    @DisplayName("should cover questionnaire status validation helper")
    void shouldCoverQuestionnaireStatusValidationHelper() throws Exception {
        assertUtilityClass(QuestionnaireStatusMachine.class);

        AuditInfo audit = AuditTestData.createdAudit();
        Question question = Question.rehydrate("question_ready", "Question ready", ParameterizationStatus.ACTIVE, "SALE", audit);
        ConfiguredQuestion configuredQuestion = ConfiguredQuestion.createNew(question, AnswerConfigurationFactory.createTextStrategy(), 1);
        Questionnaire questionnaire = Questionnaire.rehydrate(
                QuestionnaireId.of("survey_status", "channel", "journey"),
                "Description",
                ParameterizationStatus.DRAFT,
                List.of(configuredQuestion),
                audit
        );

        AuditUser actor = OrderQuestionnaireAuditFactory.user(USER_ID, "REF", "Actor", "actor@acme.com")
                .getOrElseThrow(error -> new IllegalStateException("Expected actor success: " + error));
        QuestionnaireStatusTransitionContext validContext = new QuestionnaireStatusTransitionContext(
                questionnaire,
                actor,
                UPDATED_AT,
                audit,
                false
        );

        Result<ParameterizationStatus, List<DomainError>> validTransition = QuestionnaireStatusMachine.validateTransition(
                ParameterizationStatus.DRAFT,
                ParameterizationStatus.ACTIVE,
                validContext
        );
        assertInstanceOf(Result.Success.class, validTransition);
        assertEquals(ParameterizationStatus.ACTIVE,
                validTransition.getOrElseThrow(error -> new IllegalStateException("Expected success: " + error)));

        QuestionnaireStatusTransitionContext missingOccurredAt = new QuestionnaireStatusTransitionContext(
                questionnaire,
                actor,
                null,
                audit,
                false
        );
        Result<TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext>, List<DomainError>> invalidTransition =
                QuestionnaireStatusMachine.transition(ParameterizationStatus.DRAFT, ParameterizationStatus.INACTIVE, missingOccurredAt);
        assertInstanceOf(Result.Failure.class, invalidTransition);

        QuestionnaireStatusTransitionContext nullQuestionnaireContext = new QuestionnaireStatusTransitionContext(
                null,
                actor,
                UPDATED_AT,
                audit,
                false
        );
        Result<TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext>, List<DomainError>> activationWithoutQuestionnaire =
                QuestionnaireStatusMachine.transition(ParameterizationStatus.DRAFT, ParameterizationStatus.ACTIVE, nullQuestionnaireContext);
        assertInstanceOf(Result.Failure.class, activationWithoutQuestionnaire);
    }

    private static void injectBuilderError(Object builder, DomainError error) throws Exception {
        Field errorsField = builder.getClass().getDeclaredField("errors");
        errorsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<DomainError> errors = (List<DomainError>) errorsField.get(builder);
        errors.add(error);
    }

    private static void assertUtilityClass(Class<?> type) throws Exception {
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

    private static AuditInfo audit() {
        return AuditTestData.createdAudit();
    }
}





