package com.acme.orderquestionnaire.adapters.out.mongo.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractMongoContainerIT {

    @Container
    protected static final GenericContainer<?> MONGO =
            new GenericContainer<>(DockerImageName.parse("mongo:7.0"))
                    .withExposedPorts(27017);

    @DynamicPropertySource
    static void configureMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> "mongodb://"
                + MONGO.getHost() + ":" + MONGO.getMappedPort(27017)
                + "/orderquestionnaire_test?directConnection=true");
    }
}

