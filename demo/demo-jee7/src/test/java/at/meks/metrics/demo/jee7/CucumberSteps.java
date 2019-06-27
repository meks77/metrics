package at.meks.metrics.demo.jee7;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.containsString;

public class CucumberSteps {


    private GenericContainer helloWorld;

    @After
    public void shutdownAndCleanup() {
        if (helloWorld != null) {
            if (helloWorld.isRunning()) {
                helloWorld.stop();
            }
            helloWorld.close();
        }
    }

    @SuppressWarnings("squid:S2095")
    @Given("the new deployed application")
    public void startApplication() {
        // the container is stopped after each scenario. If try finally would be used, the container wouldn't be
        // available in the scenario because directly after the step it would be closed
        helloWorld = new GenericContainer("test/demo-jee7:latest")
                .withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/jee7").forStatusCode(403).withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES)));
        helloWorld.start();
    }

    @When("employees are requested {int} times")
    public void employeesAreRequestedTimes(int times) {
        for (int i=0; i<times; i++)
            RestAssured.when().get(getUrl("jee7/api/v1.0/employee/15")).then().statusCode(200);
    }

    @When("offices of employee are request {int} times")
    public void officesOfEmployeeAreRequestTimes(int times) {
        for (int i=0; i<times; i++)
            RestAssured.when().get(getUrl("jee7/api/v1.0/employee/office/15")).then().statusCode(200);
    }

    private String getUrl(String relativeUrl) {
        Integer port = helloWorld.getFirstMappedPort();
        return "http://localhost:" + port + "/" + relativeUrl;
    }

    @Then("the counter of the employee-requests was increased to {int}")
    public void theCounterOfTheEmployeeRequestsWasIncreasedTo(int expectedTimes) {
        RestAssured.when().get(getUrl("jee7/metrics")).then().contentType(ContentType.TEXT)
                .body(containsString(getExpectedCounterMetricsLine(expectedTimes, "getEmployee")));
    }

    private String getExpectedCounterMetricsLine(int expectedTimes, String methodName) {
        return "method_execution_counter{" +
                "class_name=\"at.meks.metrics.demo.application.EmployeeService\"," +
                "method_name=\""+methodName+"\"," +
                "method_args=\"java.lang.String\",} " +
                ""+expectedTimes+".0";

    }

    @Then("the counter of the office-requests was increased to {int}")
    public void theCounterOfTheOfficeRequestsWasIncreasedTo(int expectedTimes) {
        RestAssured.when().get(getUrl("jee7/metrics")).then().contentType(ContentType.TEXT)
                .body(containsString(getExpectedCounterMetricsLine(expectedTimes, "getOfficeOfEmployee")));
    }
}
