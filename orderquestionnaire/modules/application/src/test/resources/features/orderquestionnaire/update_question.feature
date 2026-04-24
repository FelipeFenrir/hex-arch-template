Feature: Update question
  As an application consumer
  I want to update question data and status
  So that question lifecycle rules are enforced

  Scenario: Successfully update question fields without changing status
    Given an existing question "question_one" with status "DRAFT"
    And an update question request for id "question_one" with label "New label", sales item "SKU-NEW" and status "KEEP"
    When I execute the update question use case
    Then the update question result should be a success
    And the updated question view should have status "DRAFT" and label "New label"

  Scenario: Fail to update question with invalid status transition
    Given an existing question "question_three" with status "DRAFT"
    And an update question request for id "question_three" with label "Question", sales item "SKU-1" and status "INACTIVE"
    When I execute the update question use case
    Then the update question result should fail with code "INVALID_STATUS_TRANSITION"

  Scenario: Fail to update question when it does not exist
    Given no existing question "missing" for update
    And an update question request for id "missing" with label "Question", sales item "SKU-1" and status "KEEP"
    When I execute the update question use case
    Then the update question result should fail with code "QUESTION_NOT_FOUND"

