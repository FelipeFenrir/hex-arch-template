Feature: Create questionnaire
  As an application consumer
  I want to create questionnaires with validation and business checks
  So that invalid or duplicated questionnaires are rejected

  Scenario: Create questionnaire successfully
    Given channel "APP" exists for questionnaire creation
    And journey "JOURNEY_01" exists for questionnaire creation
    And no questionnaire exists with id "q_001", channel "APP" and journey "JOURNEY_01"
    And a create questionnaire command with id "q_001", channel "APP", journey "JOURNEY_01" and description "Questionnaire description"
    When I execute the create questionnaire use case
    Then the create questionnaire result should be a success
    And the created questionnaire view should have id "q_001" and status "DRAFT"

  Scenario: Reject create questionnaire when it already exists
    Given channel "APP" exists for questionnaire creation
    And journey "JOURNEY_01" exists for questionnaire creation
    And questionnaire already exists with id "q_001", channel "APP" and journey "JOURNEY_01"
    And a create questionnaire command with id "q_001", channel "APP", journey "JOURNEY_01" and description "Questionnaire description"
    When I execute the create questionnaire use case
    Then the create questionnaire result should fail with code "QUESTIONNAIRE_ALREADY_EXISTS"

  Scenario: Reject create questionnaire when audit info is missing
    Given channel "APP" exists for questionnaire creation
    And journey "JOURNEY_01" exists for questionnaire creation
    And no questionnaire exists with id "q_001", channel "APP" and journey "JOURNEY_01"
    And a create questionnaire command with id "q_001", channel "APP", journey "JOURNEY_01", description "Questionnaire description" and missing audit info
    When I execute the create questionnaire use case
    Then the create questionnaire result should fail with code "INVALID_USER_ID"

  Scenario: Reject create questionnaire when channel does not exist
    Given channel "APP" does not exist for questionnaire creation
    And a create questionnaire command with id "q_001", channel "APP", journey "JOURNEY_01" and description "Questionnaire description"
    When I execute the create questionnaire use case
    Then the create questionnaire result should fail with code "CHANNEL_DISTRIBUTION_NOT_FOUND"
    And create questionnaire should short-circuit after channel validation

  Scenario: Reject create questionnaire when journey does not exist
    Given channel "APP" exists for questionnaire creation
    And journey "JOURNEY_01" does not exist for questionnaire creation
    And a create questionnaire command with id "q_001", channel "APP", journey "JOURNEY_01" and description "Questionnaire description"
    When I execute the create questionnaire use case
    Then the create questionnaire result should fail with code "JOURNEY_DISTRIBUTION_NOT_FOUND"
    And create questionnaire should short-circuit after journey validation


