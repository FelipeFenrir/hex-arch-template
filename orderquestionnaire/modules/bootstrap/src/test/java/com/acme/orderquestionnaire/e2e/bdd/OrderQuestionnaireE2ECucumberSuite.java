package com.acme.orderquestionnaire.e2e.bdd;

import com.acme.shared.stereotypes.test.E2ETest;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@E2ETest
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/orderquestionnaire/e2e")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.acme.orderquestionnaire.e2e.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
class OrderQuestionnaireE2ECucumberSuite {
}

