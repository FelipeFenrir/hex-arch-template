package com.acme.orderquestionnaire.application.unit;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.orderquestionnaire.application.question.dto.command.CreateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.command.UpdateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.queries.GetQuestionById;
import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionUpdatedView;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionQueryOutPort;
import com.acme.orderquestionnaire.application.unit.question.service.CreateQuestionServiceTest;
import com.acme.orderquestionnaire.application.question.service.GetQuestionByIdHandler;
import com.acme.orderquestionnaire.application.question.service.SearchQuestionByFilterHandler;
import com.acme.orderquestionnaire.application.unit.question.service.UpdateQuestionServiceTest;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.AnswerConfigParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireUpdatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.unit.questionnaire.service.CreateQuestionnaireServiceTest;
import com.acme.orderquestionnaire.application.unit.questionnaire.service.UpdateQuestionnaireServiceTest;
import com.acme.orderquestionnaire.application.questionnaire.service.ValidateQuestionnaireAnswersService;
import com.acme.orderquestionnaire.application.questionnaire.service.context.ValidateQuestionnaireAnswersPipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.FetchQuestionnaireForAnswersValidationStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateAnswersAgainstQuestionnaireStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateQuestionnaireAnswersCommandStep;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("Functional Test: Order Questionnaire Application")
public class FunctionalTest {

    // ----------------- create question ------------------------------------------------------------------------------
    @Test
    @DisplayName("When create a question, then it should be created successfully")
    void shouldCreateQuestionSuccessfully() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new CreateQuestionCommand(
                "question_one",
                "What is your favorite color?",
                "color_question",
                new AuditUserParam(
                        Id.withId("11111111-1111-1111-1111-111111111111"),
                        "user_one",
                        "User One",
                        "userone@email.com"
                ),
                fixedDate
        );

        // Define exactly which Question the out-port mock should return
        var mockedQuestion = QuestionFactory
                .rehydrate(
                        command.id(),
                        command.label(),
                        ParameterizationStatus.DRAFT,
                        "color_question",
                        audit(
                                command.createdBy().id(),
                                command.createdBy().referenceCode(),
                                command.createdBy().name(),
                                command.createdBy().email(),
                                command.createdAt())
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build();

        // Mock Out Port
        QuestionCommandOutPort mockCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockCommandOutPort.existsById(anyString())).thenReturn(false);
        when(mockCommandOutPort.create(any(Question.class)))
                .thenReturn(
                        mockedQuestion
                );

        // Initialize Use Case implementation with mocked out-port
        var useCase = CreateQuestionServiceTest.buildService(mockCommandOutPort);

        // Execute the use case
        Result<QuestionCreatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Success.class, result);
        QuestionCreatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));

        assertEquals("question_one", view.id());
        assertEquals("What is your favorite color?", view.label());
        assertEquals("DRAFT", view.status());

        // Verify interactions with the mock
        verify(mockCommandOutPort).existsById("question_one");
        verify(mockCommandOutPort).create(any(Question.class));
    }

    @Test
    @DisplayName("When create a Question, then it should validate the command and return errors for command with invalid audit-info data")
    void shouldCreateQuestionCommandWithInvalidAuditInfo() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new CreateQuestionCommand(
                "question_one",
                "What is your favorite color?",
                "color_question",
                null,
                null
        );

        // Mock Out Port
        QuestionCommandOutPort mockCommandOutPort = mock(QuestionCommandOutPort.class);

        // Initialize Use Case implementation with mocked out-port
        var useCase = CreateQuestionServiceTest.buildService(mockCommandOutPort);

        // Execute the use case
        var result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        var errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));

        // Auditoria é avaliada neste fluxo.
        assertEquals(3, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_USER_ID")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("user id must not be null or blank")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_USER_NAME")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("user name must not be null or blank")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_CREATED_AT")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("createdAt must not be null")));
    }

    @Test
    @DisplayName("When create a Question, then it should validate the command and return errors for invalid command data")
    void shouldCreateQuestionWithInvalidCommand() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new CreateQuestionCommand(
                "question_one",
                null,
                null,
                new AuditUserParam(
                        Id.withId("11111111-1111-1111-1111-111111111111"),
                        "user_one",
                        "User One",
                        "userone@email.com"
                ),
                fixedDate
        );

        // Mock Out Port
        QuestionCommandOutPort mockCommandOutPort = mock(QuestionCommandOutPort.class);

        // Initialize Use Case implementation with mocked out-port
        var useCase = CreateQuestionServiceTest.buildService(mockCommandOutPort);

        // Execute the use case
        var result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        var errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));

        // Acumula payload inválido de label + sales item
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("label must not be blank")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("salesItemReferenceCode must not be blank")));
    }

    @Test
    @DisplayName("When create a question, then it should be created failing because it already exists")
    void shouldCreateQuestionAlreadyExists() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new CreateQuestionCommand(
                "mocked_question",
                "What is your favorite color?",
                "color_question",
                new AuditUserParam(
                        Id.withId("11111111-1111-1111-1111-111111111111"),
                        "user_one",
                        "User One",
                        "userone@email.com"
                ),
                fixedDate
        );

        // Mock Out Port
        QuestionCommandOutPort mockCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockCommandOutPort.existsById("mocked_question")).thenReturn(true);

        // Initialize Use Case implementation with mocked out-port
        var useCase = CreateQuestionServiceTest.buildService(mockCommandOutPort);

        // Execute the use case
        var result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        var errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));

        // With phase 1 accumulation: QUESTION_ALREADY_EXISTS error
        assertEquals(1, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTION_ALREADY_EXISTS")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("a question with id 'mocked_question' already exists")));

        // Verify interactions with the mock
        verify(mockCommandOutPort).existsById("mocked_question");
    }

    // ----------------- update question ------------------------------------------------------------------------------
    @Test
    @DisplayName("When update a question, then it should be updated successfully")
    void shouldUpdateQuestion() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionCommand(
                "Which color do you like?",
                "color_question",
                ParameterizationStatus.ACTIVE,
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "user_two",
                        "User Two",
                        "usertwo@email.com"),
                updateDate
        );

        // Define exactly which Question the out-port mock should return
        var mockedInDBQuestion = QuestionFactory
                .rehydrate(
                        "question_one",
                        command.label(),
                        ParameterizationStatus.DRAFT,
                        "color_question",
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build();

        // Define exactly which Question the out-port mock should return
        var alteredQuestion = QuestionFactory
                .rehydrate(
                        "question_one",
                        command.label(),
                        ParameterizationStatus.ACTIVE,
                        "color_question",
                        audit(
                                command.updatedBy().id(),
                                command.updatedBy().referenceCode(),
                                command.updatedBy().name(),
                                command.updatedBy().email(),
                                command.updatedAt())
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build();

        // Mock Out Port
        QuestionCommandOutPort mockCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockCommandOutPort.findQuestionById("question_one")).thenReturn(
                mockedInDBQuestion.fold(Optional::of, error -> Optional.empty())
        );
        when(mockCommandOutPort.update(any(Question.class)))
                .thenReturn(
                        alteredQuestion
                );

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionServiceTest.buildService(mockCommandOutPort);

        // Execute the use case
        Result<QuestionUpdatedView, List<DomainError>> result = useCase.execute("question_one", command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Success.class, result);
        QuestionUpdatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));

        assertEquals("question_one", view.id());
        assertEquals("Which color do you like?", view.label());
        assertEquals("ACTIVE", view.status());

        // Verify interactions with the mock
        verify(mockCommandOutPort).findQuestionById("question_one");
        verify(mockCommandOutPort).update(any(Question.class));

    }

    @Test
    @DisplayName("When update a question, then it should be updated failing because question does not exist")
    void shouldUpdateQuestionNotFound() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionCommand(
                "Which color do you like?",
                "color_question",
                ParameterizationStatus.ACTIVE,
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "user_two",
                        "User Two",
                        "usertwo@email.com"),
                updateDate
        );

        // Mock Out Port
        QuestionCommandOutPort mockCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockCommandOutPort.findQuestionById("question_one")).thenReturn(
                Optional.empty() // Simulate not found question
        );

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionServiceTest.buildService(mockCommandOutPort);

        // Execute the use case
        Result<QuestionUpdatedView, List<DomainError>> result = useCase.execute("question_one", command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));

        // With phase 1 accumulation: QUESTION_NOT_FOUND error
        assertEquals(1, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTION_NOT_FOUND")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("question was not found")));

        // Verify interactions with the mock
        verify(mockCommandOutPort).findQuestionById("question_one");
    }

    @Test
    @DisplayName("When update a question, then it should be updated failing because of command with invalid audit-info data")
    void shouldUpdateQuestionInvalidCommandAuditInfo() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionCommand(
                "Which color do you like?",
                "color_question",
                null,
                null,
                null
        );

        // Mock Out Port
        QuestionCommandOutPort mockCommandOutPort = mock(QuestionCommandOutPort.class);

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionServiceTest.buildService(mockCommandOutPort);

        // Execute the use case
        Result<QuestionUpdatedView, List<DomainError>> result = useCase.execute("question_one", command);

        // Acumula erros de auditoria + payload de atualização
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));

        // Auditoria é avaliada neste fluxo.
        assertEquals(3, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_USER_ID")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("user id must not be null or blank")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_USER_NAME")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("user name must not be null or blank")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_UPDATED_AT")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("updatedAt must not be null")));
    }

    @Test
    @DisplayName("When update a question, then it should be updated failing because of invalid status transition")
    void shouldUpdateQuestionInvalidStatusTransition() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionCommand(
                "Which color do you like?",
                "color_question",
                ParameterizationStatus.DRAFT,
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "user_two",
                        "User Two",
                        "usertwo@email.com"),
                updateDate
        );

        // Define exactly which Question the out-port mock should return
        var mockedInDBQuestion = QuestionFactory
                .rehydrate(
                        "question_one",
                        command.label(),
                        ParameterizationStatus.ACTIVE,
                        "color_question",
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build();

        // Mock Out Port
        QuestionCommandOutPort mockCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockCommandOutPort.findQuestionById("question_one")).thenReturn(
                mockedInDBQuestion.fold(Optional::of, error -> Optional.empty())
        );

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionServiceTest.buildService(mockCommandOutPort);

        // Execute the use case
        Result<QuestionUpdatedView, List<DomainError>> result = useCase.execute("question_one", command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected success but got failure"));

        // With phase 1 accumulation: INVALID_STATUS_TRANSITION error
        assertEquals(1, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_STATUS_TRANSITION")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("Cannot transition from ACTIVE to DRAFT")));

        // Verify interactions with the mock
        verify(mockCommandOutPort).findQuestionById("question_one");
    }

    // ----------------- get question by Id ---------------------------------------------------------------------------
    @Test
    @DisplayName("When get a question by id, then it should return the question successfully")
    void shouldGetQuestionById() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint query
        var query = new GetQuestionById("question_one");

        // Define exactly which QuestionView the out-port mock should return
        var mockedQuestion = QuestionFactory
                .rehydrate(
                        "mocked_question",
                        "Mocked label",
                        ParameterizationStatus.ACTIVE,
                        "color_question",
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                fixedDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build();

        var mockedQuestionView = mockedQuestion.map(QuestionView::from).getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error)
        );

        // Mock Out Port
        QuestionQueryOutPort mockQueryOutPort = mock(QuestionQueryOutPort.class);
        when(mockQueryOutPort.findById(any(GetQuestionById.class))).thenReturn(
                Optional.ofNullable(mockedQuestionView)
        );

        // Initialize Handler implementation with mocked out-port
        var handler = new GetQuestionByIdHandler(mockQueryOutPort);

        // Execute the use case
        Optional<QuestionView> result = handler.execute(query);

        // Assert the result uses the mocked return value
        QuestionView view = result.orElseThrow(() ->
                new IllegalStateException("Expected to find a question but got empty result"));

        assertEquals("mocked_question", view.id());
        assertEquals("Mocked label", view.label());
        assertEquals("ACTIVE", view.status());

        // Verify interactions with the mock
        verify(mockQueryOutPort).findById(query);
    }

    @Test
    @DisplayName("When get a question by id, then it should return empty if question does not exist")
    void shouldGetQuestionByIdNotFound() {
        // Emulates Adapter Entrypoint query
        var query = new GetQuestionById("question_one");

        // Mock Out Port
        QuestionQueryOutPort mockQueryOutPort = mock(QuestionQueryOutPort.class);
        when(mockQueryOutPort.findById(any(GetQuestionById.class))).thenReturn(
                Optional.empty()
        );

        // Initialize Handler implementation with mocked out-port
        var handler = new GetQuestionByIdHandler(mockQueryOutPort);

        // Execute the use case
        Optional<QuestionView> result = handler.execute(query);

        // Assert the result uses the mocked return value
        assertTrue(result.isEmpty());

        // Verify interactions with the mock
        verify(mockQueryOutPort).findById(query);
    }

    // ----------------- get question by Search -----------------------------------------------------------------------
    @Test
    @DisplayName("When get a question by search, then it should return the question successfully")
    void shouldGetQuestionBySearch() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint query
        var query = new SearchQuestionByFilter(
                List.of("mocked_question"),
                null,
                null,
                ParameterizationStatus.ACTIVE,
                null,
                HybridPageRequest.ofPage(0, 10, List.of(new SortSpec("id", SortDirection.ASC)))
        );

        // Define exactly which QuestionView the out-port mock should return
        var mockedQuestion = QuestionFactory
                .rehydrate(
                        "mocked_question",
                        "Mocked label",
                        ParameterizationStatus.ACTIVE,
                        "color_question",
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                fixedDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build();

        var mockedQuestionView = mockedQuestion.map(QuestionView::from).getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error)
        );

        // Mock Out Port
        QuestionQueryOutPort mockQueryOutPort = mock(QuestionQueryOutPort.class);
        var pagedResult = PageResult.forPage(
                List.of(mockedQuestionView),
                0,
                10,
                1,
                1,
                true,
                true,
                List.of(new SortSpec("id", SortDirection.ASC))
        );
        when(mockQueryOutPort.findAll(any(SearchQuestionByFilter.class))).thenReturn(
                pagedResult
        );

        // Initialize Handler implementation with mocked out-port
        var handler = new SearchQuestionByFilterHandler(mockQueryOutPort);

        // Execute the use case
        PageResult<QuestionView> result = handler.execute(query);

        // Assert the result uses the mocked return value
        assertEquals("mocked_question", result.content().getFirst().id());
        assertEquals("Mocked label", result.content().getFirst().label());
        assertEquals("ACTIVE", result.content().getFirst().status());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1L, result.totalElements());
        assertTrue(result.appliedSort().stream().anyMatch(s -> s.field().equals("id") && s.direction() == SortDirection.ASC));

        // Verify interactions with the mock
        verify(mockQueryOutPort).findAll(query);
    }

    @Test
    @DisplayName("When get a question by search, then it should return empty if question does not exist")
    void shouldGetQuestionBySearchNotFound() {
        // Emulates Adapter Entrypoint query
        var query = new SearchQuestionByFilter(
                List.of("mocked_question"),
                null,
                null,
                ParameterizationStatus.ACTIVE,
                null,
                HybridPageRequest.ofCursor("cursor-1", 10, List.of(new SortSpec("id", SortDirection.ASC)))
        );

        // Mock Out Port
        QuestionQueryOutPort mockQueryOutPort = mock(QuestionQueryOutPort.class);
        var pagedResult = PageResult.<QuestionView>forCursor(
                List.of(),
                10,
                null,
                false,
                List.of(new SortSpec("id", SortDirection.ASC))
        );
        when(mockQueryOutPort.findAll(any(SearchQuestionByFilter.class))).thenReturn(
                pagedResult
        );

        // Initialize Handler implementation with mocked out-port
        var handler = new SearchQuestionByFilterHandler(mockQueryOutPort);

        // Execute the use case
        PageResult<QuestionView> result = handler.execute(query);

        // Assert the result uses the mocked return value
        assertTrue(result.content().isEmpty());
        assertTrue(result.appliedSort().stream().anyMatch(s -> s.field().equals("id") && s.direction() == SortDirection.ASC));

        // Verify interactions with the mock
        verify(mockQueryOutPort).findAll(query);
    }

    // ----------------- create questionnaire -------------------------------------------------------------------------
    @Test
    @DisplayName("When create a questionnaire, then it should be created successfully")
    void shouldCreateQuestionnaireSuccessfully() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new CreateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "This is a questionnaire description",
                new AuditUserParam(
                        Id.withId("11111111-1111-1111-1111-111111111111"),
                        "user_one",
                        "User One",
                        "userone@one.com.br"
                ),
                fixedDate
        );

        // Define exactly which Question the out-port mock should return
        var mockedQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.DRAFT,
                        audit(
                                command.createdBy().id(),
                                command.createdBy().referenceCode(),
                                command.createdBy().name(),
                                command.createdBy().email(),
                                command.createdAt())
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build();

        // Mock Out Port
        QuestionnaireCommandOutPort mockCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        ChannelDistributionOutPort channelDistributionOutPort =  mock(ChannelDistributionOutPort.class);
        JourneyDistributionOutPort journeyDistributionOutPort =  mock(JourneyDistributionOutPort.class);

        when(channelDistributionOutPort.existsById(anyString())).thenReturn(true);
        when(journeyDistributionOutPort.existsById(anyString())).thenReturn(true);
        when(mockCommandOutPort.existsById(any(QuestionnaireId.class))).thenReturn(false);
        when(mockCommandOutPort.create(any(Questionnaire.class)))
                .thenReturn(
                        mockedQuestionnaire
                );

        // Initialize Use Case implementation with mocked out-port
        var useCase = CreateQuestionnaireServiceTest.buildService(mockCommandOutPort, channelDistributionOutPort, journeyDistributionOutPort);

        // Execute the use case
        Result<QuestionnaireCreatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Success.class, result);
        QuestionnaireCreatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));

        assertEquals("questionnaire_one", view.id());
        assertEquals("channel_dist_1", view.channelDistributionId());
        assertEquals("journey_dist_1", view.journeyDistributionId());
        assertEquals("This is a questionnaire description", view.description());
        assertEquals("DRAFT", view.status());

        // Verify interactions with the mock
        verify(channelDistributionOutPort).existsById(anyString());
        verify(journeyDistributionOutPort).existsById(anyString());
        verify(mockCommandOutPort).existsById(any(QuestionnaireId.class));
        verify(mockCommandOutPort).create(any(Questionnaire.class));
    }

    @Test
    @DisplayName("When create a questionnaire, then it should be created failing because it already exists")
    void shouldCreateQuestionnaireAlreadyExists() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new CreateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "This is a questionnaire description",
                new AuditUserParam(
                        Id.withId("11111111-1111-1111-1111-111111111111"),
                        "user_one",
                        "User One",
                        "userone@one.com.br"
                ),
                fixedDate
        );

        // Mock Out Port
        QuestionnaireCommandOutPort mockCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        ChannelDistributionOutPort channelDistributionOutPort =  mock(ChannelDistributionOutPort.class);
        JourneyDistributionOutPort journeyDistributionOutPort =  mock(JourneyDistributionOutPort.class);

        when(channelDistributionOutPort.existsById(anyString())).thenReturn(true);
        when(journeyDistributionOutPort.existsById(anyString())).thenReturn(true);
        when(mockCommandOutPort.existsById(any(QuestionnaireId.class))).thenReturn(true);

        // Initialize Use Case implementation with mocked out-port
        var useCase = CreateQuestionnaireServiceTest.buildService(mockCommandOutPort, channelDistributionOutPort, journeyDistributionOutPort);

        // Execute the use case
        Result<QuestionnaireCreatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected success but got failure"));

        // With phase 1 accumulation: QUESTIONNAIRE_ALREADY_EXISTS error
        assertEquals(1, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTIONNAIRE_ALREADY_EXISTS")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("a questionnaire with id 'questionnaire_one' already exists")));

        // Verify interactions with the mock
        verify(channelDistributionOutPort).existsById(anyString());
        verify(journeyDistributionOutPort).existsById(anyString());
        verify(mockCommandOutPort).existsById(any(QuestionnaireId.class));
    }

    @Test
    @DisplayName("When create a questionnaire, then it should be created failing because journey distribution does not exist")
    void shouldCreateQuestionnaireJourneyNotExists() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new CreateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "This is a questionnaire description",
                new AuditUserParam(
                        Id.withId("11111111-1111-1111-1111-111111111111"),
                        "user_one",
                        "User One",
                        "userone@one.com.br"
                ),
                fixedDate
        );

        // Mock Out Port
        QuestionnaireCommandOutPort mockCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        ChannelDistributionOutPort channelDistributionOutPort =  mock(ChannelDistributionOutPort.class);
        JourneyDistributionOutPort journeyDistributionOutPort =  mock(JourneyDistributionOutPort.class);

        when(channelDistributionOutPort.existsById(anyString())).thenReturn(true);
        when(journeyDistributionOutPort.existsById(anyString())).thenReturn(false);

        // Initialize Use Case implementation with mocked out-port
        var useCase = CreateQuestionnaireServiceTest.buildService(mockCommandOutPort, channelDistributionOutPort, journeyDistributionOutPort);

        // Execute the use case
        Result<QuestionnaireCreatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected success but got failure"));

        // With phase 1 accumulation: JOURNEY_DISTRIBUTION_NOT_FOUND error
        assertEquals(1, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("JOURNEY_DISTRIBUTION_NOT_FOUND")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("journey distribution with id 'journey_dist_1' was not found")));

        // Verify interactions with the mock
        verify(channelDistributionOutPort).existsById(anyString());
        verify(journeyDistributionOutPort).existsById(anyString());
    }

    @Test
    @DisplayName("When create a questionnaire, then it should be created failing because channel distribution does not exist")
    void shouldCreateQuestionnaireChannelNotExists() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new CreateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "This is a questionnaire description",
                new AuditUserParam(
                        Id.withId("11111111-1111-1111-1111-111111111111"),
                        "user_one",
                        "User One",
                        "userone@one.com.br"
                ),
                fixedDate
        );

        // Mock Out Port
        QuestionnaireCommandOutPort mockCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        ChannelDistributionOutPort channelDistributionOutPort =  mock(ChannelDistributionOutPort.class);
        JourneyDistributionOutPort journeyDistributionOutPort =  mock(JourneyDistributionOutPort.class);

        when(channelDistributionOutPort.existsById(anyString())).thenReturn(false);

        // Initialize Use Case implementation with mocked out-port
        var useCase = CreateQuestionnaireServiceTest.buildService(mockCommandOutPort, channelDistributionOutPort, journeyDistributionOutPort);

        // Execute the use case
        Result<QuestionnaireCreatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected success but got failure"));

        // With phase 1 accumulation: CHANNEL_DISTRIBUTION_NOT_FOUND error
        assertEquals(1, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("CHANNEL_DISTRIBUTION_NOT_FOUND")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("channel distribution with id 'channel_dist_1' was not found")));

        // Verify interactions with the mock
        verify(channelDistributionOutPort).existsById(anyString());
    }

    @Test
    @DisplayName("When create a questionnaire, then it should be created and return errors for invalid command data")
    void shouldCreateQuestionnaireInvalidCommand() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new CreateQuestionnaireCommand(
                null,
                null,
                null,
                null,
                new AuditUserParam(
                        Id.withId("11111111-1111-1111-1111-111111111111"),
                        "user_one",
                        "User One",
                        "userone@one.com.br"
                ),
                fixedDate
        );

        // Mock Out Port
        QuestionnaireCommandOutPort mockCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        ChannelDistributionOutPort channelDistributionOutPort =  mock(ChannelDistributionOutPort.class);
        JourneyDistributionOutPort journeyDistributionOutPort =  mock(JourneyDistributionOutPort.class);

        // Initialize Use Case implementation with mocked out-port
        var useCase = CreateQuestionnaireServiceTest.buildService(mockCommandOutPort, channelDistributionOutPort, journeyDistributionOutPort);

        // Execute the use case
        Result<QuestionnaireCreatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected success but got failure"));

        // With domain-owned payload validation: REQUIRED_FIELD errors for id/channel/journey/description
        assertEquals(4, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD") && e.message().equals("id must not be blank")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD") && e.message().equals("channelDistributionId must not be blank")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD") && e.message().equals("journeyDistributionId must not be blank")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD") && e.message().equals("description must not be blank")));
    }

    @Test
    @DisplayName("When create a questionnaire, then it should be created and return errors for invalid audit-info data")
    void shouldCreateQuestionnaireInvalidCommandAuditInfo() {
        // Init fixed values
        LocalDateTime fixedDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new CreateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "This is a questionnaire description",
                null,
                null
        );

        // Mock Out Port
        QuestionnaireCommandOutPort mockCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        ChannelDistributionOutPort channelDistributionOutPort =  mock(ChannelDistributionOutPort.class);
        JourneyDistributionOutPort journeyDistributionOutPort =  mock(JourneyDistributionOutPort.class);

        // Initialize Use Case implementation with mocked out-port
        var useCase = CreateQuestionnaireServiceTest.buildService(mockCommandOutPort, channelDistributionOutPort, journeyDistributionOutPort);

        // Execute the use case
        Result<QuestionnaireCreatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected success but got failure"));

        // With phase 4 accumulation: INVALID_USER_ID and INVALID_USER_NAME and INVALID_CREATED_AT error
        assertEquals(3, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_USER_ID")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("user id must not be null")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_USER_NAME")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("user name must not be null or blank")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_CREATED_AT")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("createdAt must not be null")));
    }

    // ----------------- update questionnaire -------------------------------------------------------------------------

    @Test
    @DisplayName("When update a questionnaire, then it should be updated successfully")
    void shouldUpdateQuestionnaireSuccessfully() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "New questionnaire description",
                ParameterizationStatus.DRAFT,
                null,
                null,
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "user_two",
                        "User Two",
                        ""),
                updateDate
        );

        // Define exactly which Question the out-port mock should return
        var mockedDbQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.DRAFT,
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build()
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                );

        // Define exactly which Question the out-port mock should return
        var alteredQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.DRAFT,
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate
                        ).withUpdate(
                                OrderQuestionnaireAuditFactory.user(
                                        command.updatedBy().id(),
                                        command.updatedBy().referenceCode(),
                                        command.updatedBy().name(),
                                        command.updatedBy().email()
                                ).getOrElseThrow(error ->
                                        new IllegalStateException("Expected success but got failure: " + error)
                                ),
                                command.updatedAt()
                        )
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build();


        // Mock Out Port
        QuestionnaireCommandOutPort mockQuestionnaireCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        QuestionCommandOutPort mockQuestionCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockQuestionnaireCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(
                        Optional.of(mockedDbQuestionnaire)
                );
        when(mockQuestionnaireCommandOutPort.update(any(Questionnaire.class)))
                .thenReturn(
                        alteredQuestionnaire
                );

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionnaireServiceTest.buildService(mockQuestionnaireCommandOutPort, mockQuestionCommandOutPort);

        // Execute the use case
        Result<QuestionnaireUpdatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Success.class, result);
        QuestionnaireUpdatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));

        assertEquals("questionnaire_one", view.id());
        assertEquals("channel_dist_1", view.channelDistributionId());
        assertEquals("journey_dist_1", view.journeyDistributionId());
        assertEquals("New questionnaire description", view.description());
        assertEquals("DRAFT", view.status());

        // Verify interactions with the mock
        verify(mockQuestionnaireCommandOutPort).findQuestionnaireById(any(QuestionnaireId.class));
        verify(mockQuestionnaireCommandOutPort).update(any(Questionnaire.class));
    }

    @Test
    @DisplayName("When update a questionnaire, a error result is returned if the questionnaire does not exist.")
    void shouldUpdateQuestionnaireNotExist() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "New questionnaire description",
                ParameterizationStatus.DRAFT,
                null,
                null,
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "user_two",
                        "User Two",
                        ""),
                updateDate
        );

        // Mock Out Port
        QuestionnaireCommandOutPort mockQuestionnaireCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        QuestionCommandOutPort mockQuestionCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockQuestionnaireCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(
                        Optional.empty()
                );

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionnaireServiceTest.buildService(mockQuestionnaireCommandOutPort, mockQuestionCommandOutPort);

        // Execute the use case
        Result<QuestionnaireUpdatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected success but got failure"));

        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTIONNAIRE_NOT_FOUND")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("questionnaire 'questionnaire_one' was not found")));

        // Verify interactions with the mock
        verify(mockQuestionnaireCommandOutPort).findQuestionnaireById(any(QuestionnaireId.class));
    }

    @Test
    @DisplayName("When update a questionnaire, a error results is returned if the questionnaire is activating with no questions")
    void shouldActiveQuestionnaireWithNoQuestions() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "New questionnaire description",
                ParameterizationStatus.ACTIVE,
                null,
                null,
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "user_two",
                        "User Two",
                        "usertwo@two.com.br"),
                updateDate
        );

        // Define exactly which Question the out-port mock should return
        var mockedDbQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.DRAFT,
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build()
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                );


        // Mock Out Port
        QuestionnaireCommandOutPort mockQuestionnaireCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        QuestionCommandOutPort mockQuestionCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockQuestionnaireCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(
                        Optional.of(mockedDbQuestionnaire)
                );

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionnaireServiceTest.buildService(mockQuestionnaireCommandOutPort, mockQuestionCommandOutPort);

        // Execute the use case
        Result<QuestionnaireUpdatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected success but got failure"));

        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_CONFIGURED_QUESTION_FOR_ACTIVATION")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("questionnaire must have at least one configured question with answerConfiguration and order")));

        // Verify interactions with the mock
        verify(mockQuestionnaireCommandOutPort).findQuestionnaireById(any(QuestionnaireId.class));
    }

    @Test
    @DisplayName("When update a questionnaire, a error results is returned if thw questionnaire is inactivating wit wrong status")
    void shouldUpdateQuestionnaireWrongStatus() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "New questionnaire description",
                ParameterizationStatus.DRAFT,
                null,
                null,
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "user_two",
                        "User Two",
                        ""),
                updateDate
        );

        // Define exactly which Question the out-port mock should return
        var mockedDbQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.ACTIVE,
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build()
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                );

        // Mock Out Port
        QuestionnaireCommandOutPort mockQuestionnaireCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        QuestionCommandOutPort mockQuestionCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockQuestionnaireCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(
                        Optional.of(mockedDbQuestionnaire)
                );

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionnaireServiceTest.buildService(mockQuestionnaireCommandOutPort, mockQuestionCommandOutPort);

        // Execute the use case
        Result<QuestionnaireUpdatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected success but got failure"));

        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTIONNAIRE_UPDATE_NOT_ALLOWED")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("questionnaire can only be structurally updated while in DRAFT or INACTIVE")));


        // Verify interactions with the mock
        verify(mockQuestionnaireCommandOutPort).findQuestionnaireById(any(QuestionnaireId.class));
    }

    @Test
    @DisplayName("When update a questionnaire by adding questions, a success result is returned.")
    void shouldUpdateQuestionnaireByAddingQuestions() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "New questionnaire description",
                ParameterizationStatus.ACTIVE,
                List.of(new UpdateConfiguredQuestionParam(
                        "question_one",
                        new ConfiguredQuestionParam(
                                1,
                                new AnswerConfigParam.Text("^[a-zA-Z]+$", "Only letters are allowed"),
                                null
                        )
                )),
                null,
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "user_two",
                        "User Two",
                        "usertwo@two.com.br"),
                updateDate
        );

        // Define exactly which Question the out-port mock should return
        var mockedInDBQuestion = QuestionFactory
                .rehydrate(
                        "question_one",
                        "Question one label",
                        ParameterizationStatus.DRAFT,
                        "color_question",
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build()
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                );

        // Define exactly which Question the out-port mock should return
        var mockedConfiguredQuestion = ConfiguredQuestion.createNew(
                mockedInDBQuestion,
                AnswerConfigurationFactory.createTextStrategy(
                        "^[a-zA-Z]+$",
                        "Only letters are allowed"
                ),
                1
        );

        // Define exactly which Question the out-port mock should return
        var mockedDbQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.DRAFT,
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build()
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                );

        // Define exactly which Question the out-port mock should return
        var alteredQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.ACTIVE,
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate
                        ).withUpdate(
                                OrderQuestionnaireAuditFactory.user(
                                        command.updatedBy().id(),
                                        command.updatedBy().referenceCode(),
                                        command.updatedBy().name(),
                                        command.updatedBy().email()
                                ).getOrElseThrow(error ->
                                        new IllegalStateException("Expected success but got failure: " + error)
                                ),
                                command.updatedAt()
                        )
                )
                .flatMap(builder -> builder
                        .withQuestion(
                                mockedConfiguredQuestion
                        ).build()
                );


        // Mock Out Port
        QuestionnaireCommandOutPort mockQuestionnaireCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        QuestionCommandOutPort mockQuestionCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockQuestionnaireCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(
                        Optional.of(mockedDbQuestionnaire)
                );
        when(mockQuestionCommandOutPort.findQuestionById("question_one"))
                .thenReturn(
                        Optional.of(mockedInDBQuestion)
                );
        when(mockQuestionnaireCommandOutPort.update(any(Questionnaire.class)))
                .thenReturn(
                        alteredQuestionnaire
                );

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionnaireServiceTest.buildService(mockQuestionnaireCommandOutPort, mockQuestionCommandOutPort);

        // Execute the use case
        Result<QuestionnaireUpdatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Success.class, result);
        QuestionnaireUpdatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));

        assertEquals("questionnaire_one", view.id());
        assertEquals("channel_dist_1", view.channelDistributionId());
        assertEquals("journey_dist_1", view.journeyDistributionId());
        assertEquals("New questionnaire description", view.description());
        assertEquals("ACTIVE", view.status());
        //assertEquals(1, view.questions().size());
        //assertEquals("question_one", view.questions().getFirst().questionId());

        // Verify interactions with the mock
        verify(mockQuestionnaireCommandOutPort).findQuestionnaireById(any(QuestionnaireId.class));
        verify(mockQuestionCommandOutPort).findQuestionById("question_one");
        verify(mockQuestionnaireCommandOutPort).update(any(Questionnaire.class));
    }

    @Test
    @DisplayName("When update a questionnaire by adding and removing questions in same time, a success result is returned.")
    void shouldUpdateQuestionnaireByAddingAndRemovingQuestionsInSameTime() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "New questionnaire description",
                ParameterizationStatus.ACTIVE,
                List.of(new UpdateConfiguredQuestionParam(
                        "question_one",
                        new ConfiguredQuestionParam(
                                1,
                                new AnswerConfigParam.Text("^[a-zA-Z]+$", "Only letters are allowed"),
                                null
                        )
                )),
                List.of("question_one"),
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "user_two",
                        "User Two",
                        "usertwo@two.com.br"),
                updateDate
        );

        // Define exactly which Question the out-port mock should return
        var mockedInDBQuestion = QuestionFactory
                .rehydrate(
                        "question_one",
                        "Question one label",
                        ParameterizationStatus.DRAFT,
                        "color_question",
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build()
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                );

        // Define exactly which Question the out-port mock should return
        var mockedConfiguredQuestion = ConfiguredQuestion.createNew(
                mockedInDBQuestion,
                AnswerConfigurationFactory.createTextStrategy(
                        "^[a-zA-Z]+$",
                        "Only letters are allowed"
                ),
                1
        );

        // Define exactly which Question the out-port mock should return
        var mockedDbQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.DRAFT,
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build()
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                );

        // Define exactly which Question the out-port mock should return
        var alteredQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.ACTIVE,
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate
                        ).withUpdate(
                                OrderQuestionnaireAuditFactory.user(
                                        command.updatedBy().id(),
                                        command.updatedBy().referenceCode(),
                                        command.updatedBy().name(),
                                        command.updatedBy().email()
                                ).getOrElseThrow(error ->
                                        new IllegalStateException("Expected success but got failure: " + error)
                                ),
                                command.updatedAt()
                        )
                )
                .flatMap(builder -> builder
                        .withQuestion(
                                mockedConfiguredQuestion
                        ).build()
                );


        // Mock Out Port
        QuestionnaireCommandOutPort mockQuestionnaireCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        QuestionCommandOutPort mockQuestionCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockQuestionnaireCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(
                        Optional.of(mockedDbQuestionnaire)
                );
        when(mockQuestionCommandOutPort.findQuestionById("question_one"))
                .thenReturn(
                        Optional.of(mockedInDBQuestion)
                );
        when(mockQuestionnaireCommandOutPort.update(any(Questionnaire.class)))
                .thenReturn(
                        alteredQuestionnaire
                );

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionnaireServiceTest.buildService(mockQuestionnaireCommandOutPort, mockQuestionCommandOutPort);

        // Execute the use case
        Result<QuestionnaireUpdatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Success.class, result);
        QuestionnaireUpdatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));

        assertEquals("questionnaire_one", view.id());
        assertEquals("channel_dist_1", view.channelDistributionId());
        assertEquals("journey_dist_1", view.journeyDistributionId());
        assertEquals("New questionnaire description", view.description());
        assertEquals("ACTIVE", view.status());
        //assertEquals(1, view.questions().size());
        //assertEquals("question_one", view.questions().getFirst().questionId());

        // Verify interactions with the mock
        verify(mockQuestionnaireCommandOutPort).findQuestionnaireById(any(QuestionnaireId.class));
        verify(mockQuestionCommandOutPort).findQuestionById("question_one");
        verify(mockQuestionnaireCommandOutPort).update(any(Questionnaire.class));
    }

    @Test
    @DisplayName("When update a questionnaire by adding questions, a error result is returned if the question does not exist.")
    void shouldUpdateQuestionnaireByAddingQuestionNotExist() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "New questionnaire description",
                ParameterizationStatus.ACTIVE,
                List.of(new UpdateConfiguredQuestionParam(
                        "question_one",
                        new ConfiguredQuestionParam(
                                1,
                                new AnswerConfigParam.Text("^[a-zA-Z]+$", "Only letters are allowed"),
                                null
                        )
                )),
                null,
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "user_two",
                        "User Two",
                        "usertwo@two.com.br"),
                updateDate
        );

        // Define exactly which Question the out-port mock should return
        var mockedDbQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.DRAFT,
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build()
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                );


        // Mock Out Port
        QuestionnaireCommandOutPort mockQuestionnaireCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        QuestionCommandOutPort mockQuestionCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockQuestionnaireCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(
                        Optional.of(mockedDbQuestionnaire)
                );
        when(mockQuestionCommandOutPort.findQuestionById("question_one"))
                .thenReturn(
                        Optional.empty()
                );

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionnaireServiceTest.buildService(mockQuestionnaireCommandOutPort, mockQuestionCommandOutPort);

        // Execute the use case
        Result<QuestionnaireUpdatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected success but got failure"));

        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTION_NOT_FOUND")));
        assertTrue(errors.stream().anyMatch(e -> e.message().equals("question was not found")));

        // Verify interactions with the mock
        verify(mockQuestionnaireCommandOutPort).findQuestionnaireById(any(QuestionnaireId.class));
        verify(mockQuestionCommandOutPort).findQuestionById("question_one");
    }

    @Test
    @DisplayName("When update a questionnaire by removing questions, a success result is returned.")
    void shouldUpdateQuestionnaireByRemovingQuestions() {
        // Init fixed values
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);
        LocalDateTime updateDate = LocalDateTime.of(2026, 1, 30, 0, 0);

        // Emulates Adapter Entrypoint command
        var command = new UpdateQuestionnaireCommand(
                "questionnaire_one",
                "channel_dist_1",
                "journey_dist_1",
                "New questionnaire description",
                ParameterizationStatus.ACTIVE,
                null,
                List.of("question_one"),
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "user_two",
                        "User Two",
                        "usertwo@two.com.br"),
                updateDate
        );

        // Define exactly which Question the out-port mock should return
        var mockedInDBQuestionOne = QuestionFactory
                .rehydrate(
                        "question_one",
                        "Question one label",
                        ParameterizationStatus.ACTIVE,
                        "color_question",
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .flatMap(builder -> builder
                        .build()
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                );

        var mockedInDBQuestionTwo = QuestionFactory
                .rehydrate(
                        "question_two",
                        "Question two label",
                        ParameterizationStatus.ACTIVE,
                        "color_question",
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .flatMap(builder -> builder
                        .build()
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                );

        // Define exactly which Question the out-port mock should return
        var mockedConfiguredQuestionOne = ConfiguredQuestionFactory.from(mockedInDBQuestionOne)
                .flatMap(builder -> builder
                        .withOrder(1)
                        .asText(
                                new ConfiguredQuestionFactory.TextConfig(
                                                "^[a-zA-Z]+$",
                                        "Only letters are allowed"
                                )
                        )
                );

        var mockedConfiguredQuestionTwo = ConfiguredQuestionFactory.from(mockedInDBQuestionTwo)
                .flatMap(builder -> builder
                        .withOrder(1)
                        .asText(
                                new ConfiguredQuestionFactory.TextConfig(
                                        "^[a-zA-Z]+$",
                                        "Only letters are allowed"
                                )
                        )
                );

        // Define exactly which Question the out-port mock should return
        var mockedDbQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.DRAFT,
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate)
                )
                .flatMap(builder -> builder
                    .withQuestion(mockedConfiguredQuestionOne)
                    .withQuestion(mockedConfiguredQuestionTwo)
                    .build()
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                );

        // Define exactly which Question the out-port mock should return
        var alteredQuestionnaire = QuestionnaireFactory
                .rehydrate(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        ParameterizationStatus.ACTIVE,
                        audit(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "user_one",
                                "User One",
                                "userone@email.com",
                                createDate
                        ).withUpdate(
                                OrderQuestionnaireAuditFactory.user(
                                        command.updatedBy().id(),
                                        command.updatedBy().referenceCode(),
                                        command.updatedBy().name(),
                                        command.updatedBy().email()
                                ).getOrElseThrow(error ->
                                        new IllegalStateException("Expected success but got failure: " + error)
                                ),
                                command.updatedAt()
                        )
                )
                .getOrElseThrow(error ->
                        new IllegalStateException("Expected success but got failure: " + error)
                )
                .build();


        // Mock Out Port
        QuestionnaireCommandOutPort mockQuestionnaireCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        QuestionCommandOutPort mockQuestionCommandOutPort = mock(QuestionCommandOutPort.class);
        when(mockQuestionnaireCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(
                        Optional.of(mockedDbQuestionnaire)
                );
        when(mockQuestionnaireCommandOutPort.update(any(Questionnaire.class)))
                .thenReturn(
                        alteredQuestionnaire
                );

        // Initialize Use Case implementation with mocked out-port
        var useCase = UpdateQuestionnaireServiceTest.buildService(mockQuestionnaireCommandOutPort, mockQuestionCommandOutPort);

        // Execute the use case
        Result<QuestionnaireUpdatedView, List<DomainError>> result = useCase.execute(command);

        // Assert the result uses the mocked return value
        assertInstanceOf(Result.Success.class, result);
        QuestionnaireUpdatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));

        assertEquals("questionnaire_one", view.id());
        assertEquals("channel_dist_1", view.channelDistributionId());
        assertEquals("journey_dist_1", view.journeyDistributionId());
        assertEquals("New questionnaire description", view.description());
        assertEquals("ACTIVE", view.status());

        // Verify interactions with the mock
        verify(mockQuestionnaireCommandOutPort).findQuestionnaireById(any(QuestionnaireId.class));
        verify(mockQuestionnaireCommandOutPort).update(any(Questionnaire.class));
    }

    // ----------------- validate questionnaire answers ---------------------------------------------------------------

    @Test
    @DisplayName("When validating answers and all answers are correct, then result is valid=true with no violations")
    void shouldReturnValidTrueWhenAllAnswersPassValidation() {
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        var mockedQuestionnaire = buildActiveQuestionnaireWithNumberQuestion(
                "qst_validation", "channel_dist_1", "journey_dist_1", "q_income",
                0.0, 100000.0, false, false, createDate);

        QuestionnaireCommandOutPort mockCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        when(mockCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(Optional.of(mockedQuestionnaire));

        var useCase = buildValidateQuestionnaireAnswersService(mockCommandOutPort);

        var command = new ValidateQuestionnaireAnswersCommand(
                "qst_validation", "channel_dist_1", "journey_dist_1", Map.of("q_income", 50000.0));

        var result = useCase.execute(command);

        assertInstanceOf(Result.Success.class, result);
        var view = result.getOrElseThrow(e -> new IllegalStateException("Expected success: " + e));

        assertTrue(view.valid());
        assertTrue(view.violationsByQuestionId().isEmpty());
        verify(mockCommandOutPort).findQuestionnaireById(any(QuestionnaireId.class));
    }

    @Test
    @DisplayName("When number answer violates min, then result is invalid with VALUE_BELOW_MIN and rule snapshot")
    void shouldReturnViolationForNumberAnswerBelowMin() {
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        var mockedQuestionnaire = buildActiveQuestionnaireWithNumberQuestion(
                "qst_validation", "channel_dist_1", "journey_dist_1", "q_income",
                0.0, 100000.0, false, false, createDate);

        QuestionnaireCommandOutPort mockCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        when(mockCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(Optional.of(mockedQuestionnaire));

        var useCase = buildValidateQuestionnaireAnswersService(mockCommandOutPort);

        var command = new ValidateQuestionnaireAnswersCommand(
                "qst_validation", "channel_dist_1", "journey_dist_1", Map.of("q_income", -100.0));

        var result = useCase.execute(command);

        assertInstanceOf(Result.Success.class, result);
        var view = result.getOrElseThrow(e -> new IllegalStateException("Expected success: " + e));

        assertFalse(view.valid());
        var qResult = view.violationsByQuestionId().get("q_income");
        assertNotNull(qResult);
        assertEquals(-100.0, qResult.providedAnswer());
        assertTrue(qResult.visibleByCondition());
        assertNotNull(qResult.answerRule());
        assertEquals("NUMBER", qResult.answerRule().type());
        assertEquals(0.0, ((Number) qResult.answerRule().attributes().get("min")).doubleValue(), 0.0001);
        assertFalse((Boolean) qResult.answerRule().attributes().get("allowedNegative"));
        assertTrue(qResult.violations().stream().anyMatch(v -> v.code().equals("VALUE_BELOW_MIN")));
        assertTrue(qResult.violations().stream().anyMatch(v -> v.source().equals("ANSWER_CONFIGURATION")));
    }

    @Test
    @DisplayName("When hidden question receives an answer, then ANSWER_NOT_ALLOWED_BY_CONDITION is reported with condition context")
    void shouldReturnConditionViolationWhenHiddenQuestionIsAnswered() {
        LocalDateTime createDate = LocalDateTime.of(2026, 1, 9, 0, 0);

        var maritalQ = Question.rehydrate("q_marital", "Marital status", ParameterizationStatus.ACTIVE,
                "SKU-1", audit(Id.withId("11111111-1111-1111-1111-111111111111"),
                        "REF", "User", "u@acme.com", createDate));
        var spouseQ  = Question.rehydrate("q_spouse", "Spouse name", ParameterizationStatus.ACTIVE,
                "SKU-2", audit(Id.withId("11111111-1111-1111-1111-111111111111"),
                        "REF", "User", "u@acme.com", createDate));

        var cqMarital = ConfiguredQuestion.createNew(
                maritalQ, AnswerConfigurationFactory.createTextStrategy(), 1);
        var cqSpouse  = ConfiguredQuestion.createNew(
                spouseQ, AnswerConfigurationFactory.createTextStrategy(), 2);
        cqSpouse.rootCondition(new com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition(
                "q_marital", "MARRIED"));

        var questionnaire = Questionnaire.rehydrate(
                QuestionnaireId.of("qst_validation", "channel_dist_1", "journey_dist_1"),
                "Marital questionnaire", ParameterizationStatus.ACTIVE,
                List.of(cqMarital, cqSpouse),
                audit(Id.withId("11111111-1111-1111-1111-111111111111"), "REF", "User", "u@acme.com", createDate));

        QuestionnaireCommandOutPort mockCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        when(mockCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(Optional.of(questionnaire));

        var useCase = buildValidateQuestionnaireAnswersService(mockCommandOutPort);

        var answers = new java.util.HashMap<String, Object>();
        answers.put("q_marital", "SINGLE");
        answers.put("q_spouse", "Ana");

        var command = new com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand(
                "qst_validation", "channel_dist_1", "journey_dist_1", answers);

        var result = useCase.execute(command);

        assertInstanceOf(Result.Success.class, result);
        var view = result.getOrElseThrow(e -> new IllegalStateException("Expected success: " + e));

        assertFalse(view.valid());

        var spouseResult = view.violationsByQuestionId().get("q_spouse");
        assertNotNull(spouseResult);
        assertFalse(spouseResult.visibleByCondition());
        assertNotNull(spouseResult.conditionRule());
        assertEquals("EQUAL", spouseResult.conditionRule().type());
        assertEquals("q_marital", spouseResult.conditionRule().attributes().get("questionRootCode"));
        assertTrue(spouseResult.dependsOnQuestionIds().contains("q_marital"));
        assertTrue(spouseResult.violations().stream().anyMatch(v -> v.code().equals("ANSWER_NOT_ALLOWED_BY_CONDITION")));
        assertTrue(spouseResult.violations().stream().anyMatch(v -> v.source().equals("QUESTION_CONDITION")));

        verify(mockCommandOutPort).findQuestionnaireById(any(QuestionnaireId.class));
    }

    @Test
    @DisplayName("When questionnaire is not found, then result is failure with QUESTIONNAIRE_NOT_FOUND")
    void shouldFailValidationWhenQuestionnaireNotFound() {
        QuestionnaireCommandOutPort mockCommandOutPort = mock(QuestionnaireCommandOutPort.class);
        when(mockCommandOutPort.findQuestionnaireById(any(QuestionnaireId.class)))
                .thenReturn(Optional.empty());

        var useCase = buildValidateQuestionnaireAnswersService(mockCommandOutPort);

        var command = new ValidateQuestionnaireAnswersCommand(
                "qst_missing", "channel_dist_1", "journey_dist_1", Map.of());

        var result = useCase.execute(command);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("QUESTIONNAIRE_NOT_FOUND")));
        assertTrue(errors.stream().anyMatch(e -> e.message().contains("qst_missing")));
    }

    @Test
    @DisplayName("When answers map is null, then result is failure with INVALID_ANSWERS")
    void shouldFailValidationWhenAnswersIsNull() {
        var useCase = buildValidateQuestionnaireAnswersService(
                mock(QuestionnaireCommandOutPort.class));

        var command = new ValidateQuestionnaireAnswersCommand(
                "qst_validation", "channel_dist_1", "journey_dist_1", null);

        var result = useCase.execute(command);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_ANSWERS")));
    }

    // ── FunctionalTest helpers ────────────────────────────────────────────────

    private static Questionnaire buildActiveQuestionnaireWithNumberQuestion(
            String qId, String channel, String journey, String questionId,
            Double min, Double max, boolean allowDecimal, boolean allowNegative,
            LocalDateTime createDate) {

        var question = Question.rehydrate(questionId, "Label " + questionId, ParameterizationStatus.ACTIVE,
                "SKU-1", audit(Id.withId("11111111-1111-1111-1111-111111111111"),
                        "REF", "User", "u@acme.com", createDate));

        var cq = ConfiguredQuestion.createNew(
                question,
                AnswerConfigurationFactory.createNumberStrategy(min, max, null, allowDecimal, allowNegative, null),
                1);

        return Questionnaire.rehydrate(
                QuestionnaireId.of(qId, channel, journey),
                "Survey questionnaire", ParameterizationStatus.ACTIVE, List.of(cq),
                audit(Id.withId("11111111-1111-1111-1111-111111111111"),
                        "REF", "User", "u@acme.com", createDate));
    }

    private static ValidateQuestionnaireAnswersService buildValidateQuestionnaireAnswersService(
            QuestionnaireCommandOutPort questionnaireCommandOutPort) {
        List<Step<ValidateQuestionnaireAnswersPipelineContext>> steps = List.of(
                new ValidateQuestionnaireAnswersCommandStep(),
                new FetchQuestionnaireForAnswersValidationStep(questionnaireCommandOutPort),
                new ValidateAnswersAgainstQuestionnaireStep()
        );
        return new ValidateQuestionnaireAnswersService(steps);
    }

    private static AuditInfo audit(Id id, String referenceCode, String name, String email, LocalDateTime createdAt) {
        return OrderQuestionnaireAuditFactory.createNew(id, referenceCode, name, email, createdAt)
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));
    }
}