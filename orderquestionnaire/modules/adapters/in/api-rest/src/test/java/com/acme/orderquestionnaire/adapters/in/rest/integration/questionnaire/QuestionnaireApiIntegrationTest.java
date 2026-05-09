package com.acme.orderquestionnaire.adapters.in.rest.integration.questionnaire;

import com.acme.orderquestionnaire.adapters.in.rest.error.ApiErrorCodes;
import com.acme.orderquestionnaire.adapters.in.rest.error.GlobalExceptionHandler;
import com.acme.orderquestionnaire.adapters.in.rest.error.ProblemDetailsFactory;
import com.acme.orderquestionnaire.adapters.in.rest.filters.RestHeadersFilter;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.entrypoint.QuestionnaireController;
import com.acme.orderquestionnaire.application.audit.dto.view.UserView;
import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.AnswerConfigurationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.DeleteQuestionnairesResultView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionConditionView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionConfigurationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireUpdatedView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.CreateQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.DeleteQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.UpdateQuestionnaireUseCase;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = QuestionnaireApiIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@IntegrationTest
@DisplayName("Questionnaire API Integration Test")
class QuestionnaireApiIntegrationTest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_RESPONSE =
            new ParameterizedTypeReference<>() {};

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnCollectionEnvelopeForSearch() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questionnaires?channelIds=ch1&journeyIds=jr1&page=0&size=1",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.get("data"));
        assertNotNull(body.get("meta"));
        assertNotNull(body.get("links"));
    }

    @Test
    void shouldCreateQuestionnaireWithDataEnvelope() {
        String payload = """
                {
                  "id": "qn_one",
                  "channelDistributionId": "ch1",
                  "journeyDistributionId": "jr1",
                  "description": "Questionnaire 1",
                  "createdBy": {
                    "id": "019dff07-5f02-70d4-8680-f8dc34fd5fb9",
                    "referenceCode": "seed_user",
                    "name": "Seed User",
                    "email": "seed.user@acme.com"
                  }
                }
                """;

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questionnaires",
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.get("data"));
    }

    @Test
    void shouldReturnDetailedConfiguredQuestionsForGetById() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questionnaires/qn_one?channelId=ch1&journeyId=jr1",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertNotNull(data);
        assertNotNull(data.get("configuredQuestions"));
    }

    @Test
    void shouldReturnProblemDetailsForNotFoundByCompositeKey() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questionnaires/missing?channelId=ch1&journeyId=jr1",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiErrorCodes.domain("QUESTIONNAIRE_NOT_FOUND"), body.get("code"));
        assertEquals("/api/v1/questionnaires/missing", body.get("instance"));
    }

    @Test
    void shouldReturnBadRequestWhenRequiredRequestParameterIsMissing() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questionnaires/qn_one?journeyId=jr1",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiErrorCodes.VALIDATION_INVALID_PAYLOAD, body.get("code"));
        assertEquals("/api/v1/questionnaires/qn_one", body.get("instance"));
        assertEquals("Validation error", body.get("title"));
        assertNotNull(body.get("errors"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) body.get("errors");
        assertTrue(errors.stream().anyMatch(error -> "channelId".equals(error.get("field"))));
    }

    @Test
    void shouldReturnAllMissingCompositeKeyFieldsInSingleResponse() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questionnaires/qn_one",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiErrorCodes.VALIDATION_INVALID_PAYLOAD, body.get("code"));
        assertEquals("/api/v1/questionnaires/qn_one", body.get("instance"));
        assertEquals("Validation error", body.get("title"));
        assertNotNull(body.get("errors"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) body.get("errors");
        assertTrue(errors.stream().anyMatch(error -> "channelId".equals(error.get("field"))));
        assertTrue(errors.stream().anyMatch(error -> "journeyId".equals(error.get("field"))));
    }

    @Test
    void shouldReturnValidationErrorWhenSearchBindingFails() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questionnaires?size=abc",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiErrorCodes.VALIDATION_INVALID_PAYLOAD, body.get("code"));
        assertEquals("/api/v1/questionnaires", body.get("instance"));
        assertEquals("Validation error", body.get("title"));
        assertNotNull(body.get("errors"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({QuestionnaireController.class, ProblemDetailsFactory.class, GlobalExceptionHandler.class, RestHeadersFilter.class})
    static class TestApplication {

        @Bean
        public CreateQuestionnaireUseCase createQuestionnaireUseCase() {
            return command -> Result.success(new QuestionnaireCreatedView(
                    command.id(),
                    command.channelDistributionId(),
                    command.journeyDistributionId(),
                    command.description(),
                    "DRAFT",
                    new UserView("u1", "ref", "User", "user@acme.test"),
                    LocalDateTime.now(),
                    null,
                    null
            ));
        }

        @Bean
        public UpdateQuestionnaireUseCase updateQuestionnaireUseCase() {
            return command -> Result.success(new QuestionnaireUpdatedView(
                    command.id(),
                    command.channelDistributionId(),
                    command.journeyDistributionId(),
                    command.description(),
                    command.status() == null ? "DRAFT" : command.status().name(),
                    List.of(new QuestionnaireUpdatedView.QuestionConfigurationDetailedView(
                            "q1",
                            1,
                            new AnswerConfigurationView("TEXT", Map.of("regexPattern", ".*")),
                            new QuestionConditionView("EQUAL", Map.of("questionRootCode", "q0", "expectedValue", "yes"), List.of()),
                            List.of("q0")
                    )),
                    new UserView("u1", "ref", "User", "user@acme.test"),
                    LocalDateTime.now().minusDays(1),
                    new UserView("u2", "ref2", "Updater", "updater@acme.test"),
                    LocalDateTime.now()
            ));
        }

        @Bean
        public DeleteQuestionnaireUseCase deleteQuestionnaireUseCase() {
            return new DeleteQuestionnaireUseCase() {
                @Override
                public Result<Void, List<DomainError>> execute(com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand command) {
                    return Result.success(null);
                }

                @Override
                public Result<DeleteQuestionnairesResultView, List<DomainError>> execute(List<com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand> commands) {
                    return Result.success(DeleteQuestionnairesResultView.empty());
                }
            };
        }

        @Bean(name = "getQuestionnaireByIdQueryHandler")
        public QueryHandler<GetQuestionnaireById, Optional<QuestionnaireView>> getQuestionnaireByIdQueryHandler() {
            return query -> {
                if ("missing".equals(query.questionnaireId().id())) {
                    return Optional.empty();
                }

                QuestionnaireView view = new QuestionnaireView(
                        QuestionnaireId.of(query.questionnaireId().id(), query.questionnaireId().getChannelDistributionIdValue(),
                                query.questionnaireId().getJourneyDistributionIdValue()),
                        "Questionnaire 1",
                        "DRAFT",
                        List.of(new QuestionConfigurationView(
                                new QuestionView("q1", "Question 1", "DRAFT", "sales", null, null, null, null),
                                1,
                                new AnswerConfigurationView("TEXT", Map.of("regexPattern", ".*")),
                                new QuestionConditionView("EQUAL", Map.of("questionRootCode", "q0", "expectedValue", "yes"), List.of())
                        )),
                        new UserView("u1", "ref", "User", "user@acme.test"),
                        LocalDateTime.now().minusDays(1),
                        null,
                        null
                );
                return Optional.of(view);
            };
        }

        @Bean(name = "searchQuestionnaireByFilterQueryHandler")
        public QueryHandler<SearchQuestionnaireByFilter, PageResult<QuestionnaireView>> searchQuestionnaireByFilterQueryHandler() {
            return ignoredQuery -> {
                QuestionnaireView item = new QuestionnaireView(
                        QuestionnaireId.of("qn_one", "ch1", "jr1"),
                        "Questionnaire 1",
                        "DRAFT",
                        List.of(),
                        new UserView("u1", "ref", "User", "user@acme.test"),
                        LocalDateTime.now().minusDays(1),
                        null,
                        null
                );

                if (ignoredQuery.pageRequest() != null && ignoredQuery.pageRequest().mode() == PageMode.CURSOR) {
                    return PageResult.forCursor(List.of(item), 1, "qn_one", true, List.of());
                }

                return PageResult.forPage(List.of(item), 0, 1, 1, 1, true, true, List.of());
            };
        }
    }
}

