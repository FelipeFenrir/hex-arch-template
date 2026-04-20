Feature: Questionnaire answer validation
  As a domain consumer
  I want conditional questions to validate only when visible
  So that hidden questions do not block the questionnaire flow

  Background:
    Given a new questionnaire builder for "survey_validation" channel "APP" journey "JOURNEY" and description "Validation Survey"
    And a number question "q_one" labeled "How satisfied are you?" with sales item "SALE"
    And an option list question "q_two" labeled "Would you recommend us?" with sales item "SALE" conditioned on "q_one" being greater than 7
    When I build the questionnaire from the builder

  Scenario: Validation succeeds when visible question is answered
    When I answer question "q_one" with number 8
    And I answer question "q_two" with text "yes"
    And I validate the questionnaire answers
    Then the questionnaire validation should succeed

  Scenario: Validation succeeds when conditional question is hidden
    When I answer question "q_one" with number 5
    And I validate the questionnaire answers
    Then the questionnaire validation should succeed

  Scenario: Validation fails when visible conditional question is missing
    When I answer question "q_one" with number 8
    And I validate the questionnaire answers
    Then the questionnaire validation should fail with 1 failure for question "q_two"
