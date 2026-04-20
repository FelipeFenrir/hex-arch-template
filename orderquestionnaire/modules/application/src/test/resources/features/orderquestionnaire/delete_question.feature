Feature: Delete question
  As an application consumer
  I want to delete questions safely
  So that questionnaire relationships remain consistent

  Scenario: Delete one question successfully when not referenced by questionnaire
    Given question "q_001" exists for delete
    And a delete question request for id "q_001"
    When I execute delete question
    Then delete question should succeed

  Scenario: Reject single delete when question is referenced by questionnaire
    Given question "q_linked" exists for delete
    And question "q_linked" is linked to questionnaires "qst_01,qst_02"
    And a delete question request for id "q_linked"
    When I execute delete question
    Then delete question should fail with code "QUESTION_IN_USE"

  Scenario: Batch delete returns only per-question failures and keeps processing
    Given question "q_ok" exists for delete
    And question "q_in_use" exists for delete
    And question "q_missing" does not exist for delete
    And question "q_in_use" is linked to questionnaires "qst_09"
    And a delete questions batch request with ids "q_ok,q_in_use,q_missing"
    When I execute delete questions batch
    Then delete batch should succeed with 2 failures
    And delete batch should contain question "q_in_use" with code "QUESTION_IN_USE"
    And delete batch should contain question "q_missing" with code "QUESTION_NOT_FOUND"

