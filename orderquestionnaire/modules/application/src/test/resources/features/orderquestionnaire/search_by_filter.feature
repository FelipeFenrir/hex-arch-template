Feature: Search by filter
  As an application consumer
  I want to search questions and questionnaires with page and cursor pagination
  So that I can consume paged data with sort metadata

  Scenario: Search questions with page mode
    Given a question search filter using page 0 and size 10
    And the question repository returns one paged item sorted by "id" in "ASC"
    When I execute the question search handler
    Then the paged result mode should be "PAGE"
    And the paged result should contain 1 item
    And the applied sort should include field "id" and direction "ASC"

  Scenario: Search questions with cursor mode
    Given a question search filter using cursor "cursor-1" and size 5
    And the question repository returns cursor "cursor-2" with hasNext true sorted by "createdAt" in "DESC"
    When I execute the question search handler
    Then the paged result mode should be "CURSOR"
    And the next cursor should be "cursor-2"
    And has next should be true

  Scenario: Search questionnaires with page mode
    Given a questionnaire search filter using page 1 and size 20
    And the questionnaire repository returns one paged item sorted by "description" in "ASC"
    When I execute the questionnaire search handler
    Then the paged result mode should be "PAGE"
    And the paged result should contain 1 item
    And the applied sort should include field "description" and direction "ASC"

  Scenario: Search questionnaires with cursor mode
    Given a questionnaire search filter using cursor "cursor-q-1" and size 7
    And the questionnaire repository returns cursor "cursor-q-2" with hasNext true sorted by "updatedAt" in "DESC"
    When I execute the questionnaire search handler
    Then the paged result mode should be "CURSOR"
    And the next cursor should be "cursor-q-2"
    And has next should be true

  Scenario: Reject invalid hybrid pagination request
    When I create a hybrid page request with page 1 and cursor "cursor-1"
    Then the hybrid request creation should fail with message "page must be null when cursor is informed"


