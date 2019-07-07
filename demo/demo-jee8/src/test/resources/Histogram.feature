Feature: Histogram (as described in Prometheus) of executions of annotated methods

  The "durations less equal) are based on the duration of the service calls on my laptop with the following config
  * Intel core i5-6200U
  * 8 GB RAM
  * Windows 10
  * Docker in VirtualBox with Ubuntu Server 19.04 with 2 CPU und 1.5GB RAM

  The question is if running the tests on other physical machines, if the result is nearly the same.

  Scenario: 2 Rest Service are executed more times
    Given the jee8 demo application
    When employees are requested with following durations
      | times | duration |
      | 30    | 0        |
      | 40    | 0.005    |
      | 15    | 0.01     |
      | 5     | 0.025    |
      | 4     | 0.05     |
      | 3     | 0.075    |
      | 2     | 1.0      |
      | 1     | 2.5      |
    And offices of employees are requested with following durations
      | times | duration |
      | 20    | 0.00     |
      | 45    | 0.005    |
      | 15    | 0.01     |
      | 10    | 0.025    |
      | 4     | 0.5      |
      | 3     | 0.075    |
      | 2     | 1.0      |
      | 1     | 2.5      |
    Then the histogram of the employee-requests differs only by 20 %
      | duration less equals | count |
      | 0.005                | 30    |
      | 0.01                 | 70    |
      | 0.025                | 85    |
      | 0.05                 | 90    |
      | 0.075                | 94    |
      | 1.0                  | 97    |
      | 2.5                  | 99    |
      | 7.5                  | 100   |
      | 10.0                 | 100   |
      | +Inf                 | 100   |
    And the histogram of the office-requests differs only by 20 %
      | duration less equals | count |
      | 0.005                | 20    |
      | 0.01                 | 65    |
      | 0.025                | 80    |
      | 0.05                 | 90    |
      | 0.075                | 94    |
      | 1.0                  | 97    |
      | 2.5                  | 99    |
      | 7.5                  | 100   |
      | 10.0                 | 100   |
      | +Inf                 | 100   |
    And the histogram count of the employee-requests is 100
    And the histogram count of the office-requests is 100
    And the histogram sum of the employee-requests is 15.2 with a deviation of 15 %
    And the histogram sum of the office-requests is 7.4 with a deviation of 15 %