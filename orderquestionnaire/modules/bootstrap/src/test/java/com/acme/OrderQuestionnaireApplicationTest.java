package com.acme;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.mongo.uri=mongodb://mongo:27017/admin"
})
class BootstrapApplicationTest {

    @Test
    void contextLoads() {
    }

}