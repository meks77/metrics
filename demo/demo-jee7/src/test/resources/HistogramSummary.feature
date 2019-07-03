Feature: Histogram(Microprofile Metrics) Summary(Prometheus) of executions of annotated methods

  The quantiles are based on the duration of the service calls on my laptop with the following config
  * Intel core i5-6200U
  * 8 GB RAM
  * Windows 10
  * Docker in VirtualBox with Ubuntu Server 19.04 with 2 CPU und 1.5GB RAM

  The question is if running the tests on other physical machines, if the result is nearly the same.

  Scenario: 2 Rest Service are executed more times
    When employees are requested with following durations
      | times | duration |
      | 30    | 0.05     |
      | 40    | 0.12     |
      | 15    | 0.20     |
      | 5     | 0.35     |
      | 4     | 0.5      |
      | 3     | 0.7      |
      | 2     | 0.9      |
      | 1     | 1.4      |
    And offices of employees are requested with following durations
      | times | duration |
      | 20    | 0.05     |
      | 45    | 0.09     |
      | 15    | 0.15     |
      | 10    | 0.20     |
      | 4     | 0.30     |
      | 3     | 0.4      |
      | 2     | 0.7      |
      | 1     | 0.9      |
    Then the summary of the employee-requests differs only by 15 %
      | quantile | duration |
      | 0.5      | 0.1205   |
      | 0.75     | 0.2005   |
      | 0.95     | 0.7005   |
      | 0.98     | 0.9005   |
      | 0.99     | 0.9005   |
      | 0.999    | 0.9005   |
    And the summary of the office-requests differs only by 15 %
      | quantile | duration |
      | 0.5      | 0.1000   |
      | 0.75     | 0.1505   |
      | 0.95     | 0.4005   |
      | 0.98     | 0.7505   |
      | 0.99     | 0.7505   |
      | 0.999    | 0.7505   |
    And the summary count of the employee-requests is 100
    And the summary count of the office-requests is 100
    And the summary sum of the employee-requests is 15.2 with a deviation of 15 %
    And the summary sum of the office-requests is 7.4 with a deviation of 15 %