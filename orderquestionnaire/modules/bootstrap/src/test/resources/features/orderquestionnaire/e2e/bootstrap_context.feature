Feature: Bootstrap critical flow

  Scenario: Start bootstrap context for critical flow baseline
    Given the orderquestionnaire bootstrap application is configured
    When the application context starts
    Then the critical bootstrap context is available

