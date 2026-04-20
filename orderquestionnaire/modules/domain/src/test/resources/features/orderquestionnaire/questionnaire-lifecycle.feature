Feature: Questionnaire lifecycle
  As a domain consumer
  I want to assemble questionnaires fluently
  So that I can validate and export them consistently

  Scenario: Create a draft questionnaire with a conditional follow-up question
    Given a new questionnaire builder for "survey_bdd" channel "APP" journey "JOURNEY" and description "BDD Survey"
    And a number question "q_one" labeled "How satisfied are you?" with sales item "SALE"
    And an option list question "q_two" labeled "Would you recommend us?" with sales item "SALE" conditioned on "q_one" being greater than 7
    When I build the questionnaire from the builder
    Then the questionnaire should be created with status "DRAFT"
    And the questionnaire should contain 2 ordered questions
    And the questionnaire tree should contain 2 questions
