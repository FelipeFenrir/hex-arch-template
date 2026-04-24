Feature: Create question
  As an application consumer
  I want to create questions with consistent validations
  So that duplicate or invalid questions are rejected early

  Scenario: Create a question successfully
    Given no question exists with id "question_one" for creation
    And a create question command with id "question_one", label "Age" and sales item "SKU-1"
    When I execute the create question use case
    Then the create question result should be a success
    And the created question view should have id "question_one" and status "DRAFT"

  Scenario: Reject question creation when it already exists
    Given a question already exists with id "question_one" for creation
    And a create question command with id "question_one", label "Age" and sales item "SKU-1"
    When I execute the create question use case
    Then the create question result should fail with code "QUESTION_ALREADY_EXISTS"

  Scenario: Reject question creation when audit info is missing
    Given no question exists with id "question_one" for creation
    And a create question command with id "question_one", label "Age", sales item "SKU-1" and missing audit info
    When I execute the create question use case
    Then the create question result should fail with code "INVALID_USER_ID"

