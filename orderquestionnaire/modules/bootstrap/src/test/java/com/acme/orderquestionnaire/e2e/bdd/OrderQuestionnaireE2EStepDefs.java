package com.acme.orderquestionnaire.e2e.bdd;

import com.acme.shared.stereotypes.test.BddTestSteps;
import com.acme.shared.stereotypes.test.E2ETest;
import io.cucumber.spring.CucumberContextConfiguration;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@E2ETest
@BddTestSteps
@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
public class OrderQuestionnaireE2EStepDefs {

    @Autowired
    private ApplicationContext applicationContext;

    private boolean contextLoaded;

    @Given("the orderquestionnaire bootstrap application is configured")
    public void theApplicationIsConfigured() {
        assertNotNull(applicationContext);
    }

    @When("the application context starts")
    public void theApplicationContextStarts() {
        contextLoaded = applicationContext.getId() != null && !applicationContext.getId().isBlank();
    }

    @Then("the critical bootstrap context is available")
    public void theCriticalBootstrapContextIsAvailable() {
        assertTrue(contextLoaded);
    }
}




