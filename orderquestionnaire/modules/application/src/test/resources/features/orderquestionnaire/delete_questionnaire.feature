Feature: Delete questionnaire
  As an application consumer
  I want to delete questionnaires safely
  So that only allowed statuses are removed and question catalog remains unaffected

  Scenario: Delete one questionnaire successfully when status is DRAFT
    Given questionnaire "q_001" with channel "APP" and journey "J_1" exists for delete with status "DRAFT"
    And a delete questionnaire request for id "q_001", channel "APP", journey "J_1"
    When I execute delete questionnaire
    Then delete questionnaire should succeed

  Scenario: Reject single delete when questionnaire status is ACTIVE
    Given questionnaire "q_002" with channel "APP" and journey "J_1" exists for delete with status "ACTIVE"
    And a delete questionnaire request for id "q_002", channel "APP", journey "J_1"
    When I execute delete questionnaire
    Then delete questionnaire should fail with code "QUESTIONNAIRE_DELETE_NOT_ALLOWED"
    And delete questionnaire should fail with message "questionnaire 'q_002' cannot be deleted with status 'ACTIVE'; only DRAFT or INACTIVE are allowed"

  Scenario: Batch delete returns only per-questionnaire failures and keeps processing
    Given questionnaire "q_ok" with channel "APP" and journey "J_1" exists for delete with status "DRAFT"
    And questionnaire "q_blocked" with channel "APP" and journey "J_1" exists for delete with status "ACTIVE"
    And questionnaire "q_missing" with channel "APP" and journey "J_1" does not exist for delete
    And a delete questionnaires batch request with commands "q_ok|APP|J_1;q_blocked|APP|J_1;q_missing|APP|J_1"
    When I execute delete questionnaires batch
    Then delete questionnaires batch should succeed with 2 failures
    And delete questionnaires batch should contain questionnaire "q_blocked", channel "APP", journey "J_1" with code "QUESTIONNAIRE_DELETE_NOT_ALLOWED"
    And delete questionnaires batch should contain questionnaire "q_missing", channel "APP", journey "J_1" with code "QUESTIONNAIRE_NOT_FOUND"

  Scenario: Batch delete should fail globally for null command list
    Given a null delete questionnaires batch request
    When I execute delete questionnaires batch
    Then delete questionnaires batch should fail globally with code "INVALID_IDS"

  Scenario: Delete inactive questionnaire even with configured questions (non-cascade contract)
    Given questionnaire "q_003" with channel "APP" and journey "J_1" exists for delete with status "INACTIVE" and configured questions
    And a delete questionnaire request for id "q_003", channel "APP", journey "J_1"
    When I execute delete questionnaire
    Then delete questionnaire should succeed

