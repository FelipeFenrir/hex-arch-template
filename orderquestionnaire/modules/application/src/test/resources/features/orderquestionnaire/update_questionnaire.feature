Feature: Update questionnaire
  As an application consumer
  I want to update questionnaire data and status
  So that it follows business status and configuration rules

  Scenario: Successfully add configured question and activate questionnaire
    Given an existing questionnaire "q_001" with status "DRAFT"
    And question "q_income" exists for update
    And an update questionnaire command adding question "q_income" and status "ACTIVE"
    When I execute the update questionnaire use case
    Then the update questionnaire result should be a success
    And the update view should contain configured question "q_income"
    And the configured question "q_income" should have answer type "NUMBER"
    And the configured question "q_income" should have no dependencies
    And the questionnaire update repository call should have happened

  Scenario: Successfully add configured question with valid condition on existing questionnaire question
    Given an existing questionnaire "q_001" with status "DRAFT" already containing question "q_age"
    And question "q_income" exists for update
    And an update questionnaire command adding question "q_income" with condition on "q_age"
    When I execute the update questionnaire use case
    Then the update questionnaire result should be a success
    And the update view should contain configured question "q_income"
    And the configured question "q_income" should have answer type "NUMBER"
    And the configured question "q_income" should depend on question "q_age"
    And the questionnaire update repository call should have happened

  Scenario: Fail to activate questionnaire without configured questions
    Given an existing questionnaire "q_001" with status "DRAFT"
    And an update questionnaire command with status "ACTIVE" and no configured questions
    When I execute the update questionnaire use case
    Then the update questionnaire result should fail with error code "INVALID_CONFIGURED_QUESTION_FOR_ACTIVATION"

  Scenario: Fail active structural update
    Given an existing questionnaire "q_001" with status "ACTIVE"
    And question "q_new" exists for update
    And an update questionnaire command adding question "q_new" and status "ACTIVE"
    When I execute the update questionnaire use case
    Then the update questionnaire result should fail with error code "QUESTIONNAIRE_UPDATE_NOT_ALLOWED"

