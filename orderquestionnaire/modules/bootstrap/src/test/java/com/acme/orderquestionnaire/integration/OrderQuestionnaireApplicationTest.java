package com.acme.orderquestionnaire.integration;

import com.acme.orderquestionnaire.config.TestMongoConfiguration;
import com.acme.shared.stereotypes.test.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@IntegrationTest
@Import(TestMongoConfiguration.class)
@DisplayName("OrderQuestionnaire Bootstrap Integration Test")
class OrderQuestionnaireApplicationTest {

    @Test
    void contextLoads() {
    }

}