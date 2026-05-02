package com.acme.orderquestionnaire.adapters.out.mongo.integration.support;

import com.acme.shared.stereotypes.test.IntegrationTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@IntegrationTest
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractMongoContainerIT {

    @Container
    protected static final MongoDBContainer MONGO =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void configureMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> MONGO.getReplicaSetUrl("orderquestionnaire_test"));
    }
}
