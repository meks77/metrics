Feature: Counting the occured exceptions

  Scenario: 2 Rest Service are executed more times. The counter is increased per execution
    When employees are requested 13 times
    And employees are requested 4 times with error
    And offices of employee are request 25 times
    And offices of employee are request 6 times with error
    Then the exception counter of the employee-requests was increased to 4
    And the exception counter of the office-requests was increased to 6