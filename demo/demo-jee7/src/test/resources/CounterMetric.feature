Feature: Counting the execution of annotated methods

  Scenario: 2 Rest Service are executed more times. The counter is increased per execution
    When employees are requested 13 times
    And offices of employee are request 25 times
    Then the counter of the employee-requests was increased to 13
    And the counter of the office-requests was increased to 25