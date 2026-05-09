package com.acme.orderquestionnaire.adapters.in.rest.integration.question;

import com.acme.orderquestionnaire.adapters.in.rest.error.ApiErrorCodes;
import com.acme.orderquestionnaire.adapters.in.rest.error.GlobalExceptionHandler;
import com.acme.orderquestionnaire.adapters.in.rest.error.ProblemDetailsFactory;
import com.acme.orderquestionnaire.adapters.in.rest.filters.RestHeadersFilter;
import com.acme.orderquestionnaire.adapters.in.rest.question.entrypoint.QuestionController;
import com.acme.orderquestionnaire.application.audit.dto.view.UserView;
import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.question.dto.queries.GetQuestionById;
import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.orderquestionnaire.application.question.dto.view.DeleteQuestionsResultView;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionUpdatedView;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.orderquestionnaire.application.question.port.in.usecase.CreateQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.in.usecase.DeleteQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.in.usecase.UpdateQuestionUseCase;
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
import org.springframework.http.HttpHeaders;
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

@SpringBootTest(
        classes = QuestionApiIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@IntegrationTest
@DisplayName("Question API Integration Test")
class QuestionApiIntegrationTest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_RESPONSE =
            new ParameterizedTypeReference<>() {};

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldExposeCorsHeadersForAllowedSearchOrigin() {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("http://localhost:3000");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/questions?mode=PAGE&page=0&size=1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://localhost:3000", response.getHeaders().getAccessControlAllowOrigin());
    }

    @Test
    void shouldAnswerPreflightForAllowedCreateOrigin() {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("http://localhost:5173");
        headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
        headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,x-correlation-id");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/questions",
                HttpMethod.OPTIONS,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://localhost:5173", response.getHeaders().getAccessControlAllowOrigin());
        org.junit.jupiter.api.Assertions.assertTrue(response.getHeaders().getAccessControlAllowMethods().contains(HttpMethod.POST));
    }

    @Test
    void shouldRejectPreflightForDisallowedOrigin() {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("https://evil.example");
        headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/questions?mode=PAGE&page=0&size=1",
                HttpMethod.OPTIONS,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void shouldReturnCollectionEnvelopeForSearch() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questions?page=0&size=1",
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
    void shouldUseCursorModeWhenExplicitlyRequested() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questions?mode=CURSOR&size=1",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        Map<String, Object> meta = (Map<String, Object>) body.get("meta");
        @SuppressWarnings("unchecked")
        Map<String, Object> links = (Map<String, Object>) body.get("links");

        assertNotNull(meta);
        assertNotNull(links);
        assertEquals("CURSOR", meta.get("mode"));
        assertEquals(true, meta.get("hasNext"));
        assertNotNull(meta.get("nextCursor"));
        String next = (String) links.get("next");
        assertNotNull(next);
        org.junit.jupiter.api.Assertions.assertTrue(next.contains("mode=CURSOR"));
        org.junit.jupiter.api.Assertions.assertTrue(next.contains("cursor="));
    }

    @Test
    void shouldCreateQuestionWhenAuditUserIdIsProvidedAsString() {
        String payload = """
                {
                  "id": "teste",
                  "label": "Pergunta inicial de teste",
                  "salesItemReferenceCode": "teste",
                  "createdBy": {
                    "id": "019dff07-5f02-70d4-8680-f8dc34fd5fb9",
                    "referenceCode": "seed_user",
                    "name": "Seed User",
                    "email": "seed.user@acme.com"
                  }
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questions",
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
    void shouldReturnBadRequestProblemDetailWhenBodyContainsInvalidAuditUserId() {
        String payload = """
                {
                  "id": "teste",
                  "label": "Pergunta inicial de teste",
                  "salesItemReferenceCode": "teste",
                  "createdBy": {
                    "id": "not-a-uuid",
                    "referenceCode": "seed_user",
                    "name": "Seed User",
                    "email": "seed.user@acme.com"
                  }
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questions",
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiErrorCodes.VALIDATION_BAD_REQUEST, body.get("code"));
        assertEquals("/api/v1/questions", body.get("instance"));
    }

    @Test
    void shouldReturnMalformedBodyProblemDetailWhenAuditUserIdIsNotAString() {
        String payload = """
                {
                  "id": "teste",
                  "label": "Pergunta inicial de teste",
                  "salesItemReferenceCode": "teste",
                  "createdBy": {
                    "id": { "value": "019dff07-5f02-70d4-8680-f8dc34fd5fb9" },
                    "referenceCode": "seed_user",
                    "name": "Seed User",
                    "email": "seed.user@acme.com"
                  }
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questions",
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiErrorCodes.VALIDATION_MALFORMED_BODY, body.get("code"));
        assertEquals("/api/v1/questions", body.get("instance"));
    }

    @Test
    void shouldReturnProblemDetailsForNotFound() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questions/missing",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiErrorCodes.domain("QUESTION_NOT_FOUND"), body.get("code"));
        assertEquals("/api/v1/questions/missing", body.get("instance"));
    }

    @Test
    void shouldReturnDataEnvelopeForDeleteById() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/questions/q1",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                MAP_RESPONSE
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.get("data"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({QuestionController.class, ProblemDetailsFactory.class, GlobalExceptionHandler.class, RestHeadersFilter.class})
    static class TestApplication {

        @Bean
        public CreateQuestionUseCase createQuestionUseCase() {
            return command -> Result.success(new QuestionCreatedView(
                    command.id(),
                    command.label(),
                    "DRAFT",
                    command.salesItemReferenceCode(),
                    new UserView("u1", "ref", "User", "user@acme.test"),
                    LocalDateTime.now(),
                    null,
                    null
            ));
        }

        @Bean
        public UpdateQuestionUseCase updateQuestionUseCase() {
            return (id, command) -> Result.success(new QuestionUpdatedView(
                    id,
                    command.label(),
                    command.status().name(),
                    command.salesItemReferenceCode(),
                    new UserView("u1", "ref", "User", "user@acme.test"),
                    LocalDateTime.now().minusDays(1),
                    new UserView("u2", "ref2", "Updater", "updater@acme.test"),
                    LocalDateTime.now()
            ));
        }

        @Bean
        public DeleteQuestionUseCase deleteQuestionUseCase() {
            return new DeleteQuestionUseCase() {
                @Override
                public Result<Void, List<DomainError>> execute(String id) {
                    return Result.success(null);
                }

                @Override
                public Result<DeleteQuestionsResultView, List<DomainError>> execute(List<String> ids) {
                    return Result.success(DeleteQuestionsResultView.empty());
                }
            };
        }

        @Bean(name = "getQuestionByIdQueryHandler")
        public QueryHandler<GetQuestionById, Optional<QuestionView>> getQuestionByIdQueryHandler() {
            return query -> {
                if ("missing".equals(query.id())) {
                    return Optional.empty();
                }
                return Optional.of(new QuestionView(
                        query.id(),
                        "Label",
                        "DRAFT",
                        "sales",
                        new UserView("u1", "ref", "User", "user@acme.test"),
                        LocalDateTime.now().minusDays(1),
                        null,
                        null
                ));
            };
        }

        @Bean(name = "searchQuestionByFilterQueryHandler")
        public QueryHandler<SearchQuestionByFilter, PageResult<QuestionView>> searchQuestionByFilterQueryHandler() {
            return ignoredQuery -> {
                if (ignoredQuery.pageRequest() != null && ignoredQuery.pageRequest().mode() == PageMode.CURSOR) {
                    return PageResult.forCursor(
                            List.of(new QuestionView(
                                    "q1",
                                    "Question 1",
                                    "DRAFT",
                                    "sales",
                                    new UserView("u1", "ref", "User", "user@acme.test"),
                                    LocalDateTime.now().minusDays(1),
                                    null,
                                    null
                            )),
                            1,
                            "q1",
                            true,
                            List.of()
                    );
                }

                return PageResult.forPage(
                        List.of(new QuestionView(
                                "q1",
                                "Question 1",
                                "DRAFT",
                                "sales",
                                new UserView("u1", "ref", "User", "user@acme.test"),
                                LocalDateTime.now().minusDays(1),
                                null,
                                null
                        )),
                        0,
                        1,
                        1,
                        1,
                        true,
                        true,
                        List.of()
                );
            };
        }
    }
}
