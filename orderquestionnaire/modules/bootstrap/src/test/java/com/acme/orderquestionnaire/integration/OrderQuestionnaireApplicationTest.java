package com.acme.orderquestionnaire.integration;

import com.acme.shared.stereotypes.test.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@IntegrationTest
@DisplayName("OrderQuestionnaire Bootstrap Integration Test")
class OrderQuestionnaireApplicationTest {

    @Test
    void contextLoads() {
    }

}