Feature: Validate questionnaire answers
  As an application consumer
  I want to submit a map of answers for a questionnaire and receive per-question validation feedback
  So that I know exactly which answers violated which rules and why

  Scenario: All answers are valid — returns valid=true with no violations
    Given a questionnaire "q_survey" with channel "APP" and journey "JOURNEY_01" has a number question "q_income" with min 0 max 100000 no decimals no negatives
    And the numeric answer for "q_income" is 50000.0
    When I submit the answers for validation
    Then the validation result should be a success
    And the result should be valid

  Scenario: Number answer violates min constraint — returns valid=false with VALUE_BELOW_MIN and rule snapshot
    Given a questionnaire "q_survey" with channel "APP" and journey "JOURNEY_01" has a number question "q_income" with min 0 max 100000 no decimals no negatives
    And the numeric answer for "q_income" is -5.0
    When I submit the answers for validation
    Then the validation result should be a success
    And the result should be invalid
    And question "q_income" should have violation with code "VALUE_BELOW_MIN" and source "ANSWER_CONFIGURATION"
    And question "q_income" answer rule type should be "NUMBER"
    And question "q_income" answer rule should contain attribute "min" equal to 0.0

  Scenario: Text answer does not match regex — returns valid=false with PATTERN_MISMATCH violation
    Given a questionnaire "q_survey" with channel "APP" and journey "JOURNEY_01" has a text question "q_name" with regex "^[A-Z]+$"
    And the text answer for "q_name" is "lowercase"
    When I submit the answers for validation
    Then the validation result should be a success
    And the result should be invalid
    And question "q_name" should have violation with code "PATTERN_MISMATCH" and source "ANSWER_CONFIGURATION"
    And question "q_name" answer rule type should be "TEXT"

  Scenario: Mandatory question without answer — returns MANDATORY_ANSWER violation
    Given a questionnaire "q_survey" with channel "APP" and journey "JOURNEY_01" has a number question "q_income" with min 0 max 100000 no decimals no negatives
    When I submit the answers for validation
    Then the validation result should be a success
    And the result should be invalid
    And question "q_income" should have violation with code "MANDATORY_ANSWER" and source "ANSWER_CONFIGURATION"

  Scenario: Hidden question receives an answer — returns ANSWER_NOT_ALLOWED_BY_CONDITION with condition context
    Given a questionnaire "q_survey" with channel "APP" and journey "JOURNEY_01" has question "q_marital" and conditional question "q_spouse" visible when "q_marital" equals "MARRIED"
    And the text answer for "q_marital" is "SINGLE"
    And the text answer for "q_spouse" is "Ana"
    When I submit the answers for validation
    Then the validation result should be a success
    And the result should be invalid
    And question "q_spouse" should have violation with code "ANSWER_NOT_ALLOWED_BY_CONDITION" and source "QUESTION_CONDITION"
    And question "q_spouse" should not be visible by condition
    And question "q_spouse" condition rule type should be "EQUAL"
    And question "q_spouse" depends on question "q_marital"

  Scenario: Condition satisfied and answer is correct — returns valid=true
    Given a questionnaire "q_survey" with channel "APP" and journey "JOURNEY_01" has question "q_marital" and conditional question "q_spouse" visible when "q_marital" equals "MARRIED"
    And the text answer for "q_marital" is "MARRIED"
    And the text answer for "q_spouse" is "Ana"
    When I submit the answers for validation
    Then the validation result should be a success
    And the result should be valid

  Scenario: Fail when questionnaire does not exist
    Given questionnaire "q_missing" with channel "APP" and journey "JOURNEY_01" does not exist for validation
    When I submit the answers for questionnaire "q_missing" channel "APP" journey "JOURNEY_01"
    Then the validation result should fail with error code "QUESTIONNAIRE_NOT_FOUND"

  Scenario: Fail when command is null
    Given a null validate answers command
    When I execute the validate answers use case with null command
    Then the validation result should fail with error code "INVALID_COMMAND"

  Scenario: Fail when answers map is null
    Given a questionnaire "q_survey" with channel "APP" and journey "JOURNEY_01" has a number question "q_income" with min 0 max 100000 no decimals no negatives
    When I submit null answers for questionnaire "q_survey" channel "APP" journey "JOURNEY_01"
    Then the validation result should fail with error code "INVALID_ANSWERS"

  Scenario: Option answer is invalid — returns INVALID_OPTION violation with OPTION_LIST rule snapshot
    Given a questionnaire "q_survey" with channel "APP" and journey "JOURNEY_01" has an option question "q_status" with options "ACTIVE" and "INACTIVE"
    And the text answer for "q_status" is "UNKNOWN"
    When I submit the answers for validation
    Then the validation result should be a success
    And the result should be invalid
    And question "q_status" should have violation with code "INVALID_OPTION" and source "ANSWER_CONFIGURATION"
    And question "q_status" answer rule type should be "OPTION_LIST"

  Scenario: Inactive question with answer — returns QUESTION_NOT_ACTIVE violation with QUESTION_STATUS source
    Given a questionnaire "q_survey" with channel "APP" and journey "JOURNEY_01" has an inactive number question "q_legacy" with min 0 max 100
    And the numeric answer for "q_legacy" is 50.0
    When I submit the answers for validation
    Then the validation result should be a success
    And the result should be invalid
    And question "q_legacy" should have violation with code "QUESTION_NOT_ACTIVE" and source "QUESTION_STATUS"
    And question "q_legacy" violation ruleType should be "QUESTION_STATUS"

  Scenario: Inactive question without answer — returns valid=true
    Given a questionnaire "q_survey" with channel "APP" and journey "JOURNEY_01" has an inactive number question "q_legacy" with min 0 max 100
    When I submit the answers for validation
    Then the validation result should be a success
    And the result should be valid

  Scenario: Condition references inactive question — returns CONDITION_REFERENCED_QUESTION_NOT_ACTIVE
    Given a questionnaire "q_survey" with channel "APP" and journey "JOURNEY_01" has question "q_root" and conditional question "q_dep" conditioned on inactive question "q_root"
    And the text answer for "q_dep" is "value"
    When I submit the answers for validation
    Then the validation result should be a success
    And the result should be invalid
    And question "q_dep" should have violation with code "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE" and source "QUESTION_CONDITION"

