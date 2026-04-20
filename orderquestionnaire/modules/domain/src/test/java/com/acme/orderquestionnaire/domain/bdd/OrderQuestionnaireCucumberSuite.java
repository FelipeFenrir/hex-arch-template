package com.acme.orderquestionnaire.domain.bdd;

import com.acme.shared.stereotypes.test.BddTest;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@BddTest
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/orderquestionnaire")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.acme.orderquestionnaire.domain.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
class OrderQuestionnaireCucumberSuite { }