package at.meks.metrics.demo.jee7;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class CucumberSteps {


    private static final String METHOD_GET_EMPLOYEE = "getEmployee";
    private static final String METHOD_GET_OFFICE_OF_EMPLOYEE = "getOfficeOfEmployee";

    private static final String METRICS_EXECUTION_COUNTER = "method_execution_counter";
    private static final String METRICS_EXECUTION_SUMMARY_COUNT = "method_execution_summary_count";
    private static final String METRICS_EXECUTION_SUMMARY_SUM = "method_execution_summary_sum";

    private GenericContainer jeeContainer;
    private RestServiceExecutor serviceExecutor;
    private MetricsAccessor metricsAccessor;

    @After
    public void shutdownAndCleanup() {
        if (jeeContainer != null) {
            if (jeeContainer.isRunning()) {
                jeeContainer.stop();
            }
            jeeContainer.close();
        }
    }

    @SuppressWarnings("squid:S2095")
    @Given("the new deployed application")
    public void startApplication() {
        // the container is stopped after each scenario. If try finally would be used, the container wouldn't be
        // available in the scenario because directly after the step it would be closed
        jeeContainer = new GenericContainer("test/demo-jee7:latest")
                .withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/jee7").forStatusCode(403).withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES)));
        jeeContainer.start();
        serviceExecutor = new RestServiceExecutor(jeeContainer.getFirstMappedPort());
        metricsAccessor = new MetricsAccessor(serviceExecutor);
    }

    @When("employees are requested {int} times")
    public void employeesAreRequestedTimes(int times) {
        forTimesDo(times, () -> serviceExecutor.requestEmployee());
    }

    private void forTimesDo(int times, Runnable runnable) {
        IntStream.range(0, times).forEach(index -> runnable.run());
    }

    @When("offices of employee are request {int} times")
    public void officesOfEmployeeAreRequestTimes(int times) {
        forTimesDo(times, () -> serviceExecutor.requestOffice());
    }

    @Then("^the counter of the (employee|office)-requests was increased to (\\d*)$")
    public void verifyCounterValue(String method, int expectedTimes) {
        assertThat(metricsAccessor.getMetricsValue(METRICS_EXECUTION_COUNTER, getMethodName(method)))
                .hasValue((double) expectedTimes);
    }

    @When("employees are requested with following durations")
    public void requestEmployeesWithDurations(DataTable dataTable) {
        forRowDo(dataTable, "times", this::invokeEmployeeService);
    }

    private void forRowDo(DataTable dataTable, String leftColumnHeader, BiConsumer<Double, Double> timesDurationConsumer) {
        List<Map<String, String>> durations = dataTable.asMaps();
        ExecutorService executorService = Executors.newWorkStealingPool(100);
        durations.forEach(row -> executorService.submit(() ->
                timesDurationConsumer.accept(Double.parseDouble(row.get(leftColumnHeader)),
                        Double.parseDouble(row.get("duration")))));
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void invokeEmployeeService(double times, double duration) {
        forTimesDo((int) times, () -> serviceExecutor.requestEmployee(duration));
    }

    @When("offices of employees are requested with following durations")
    public void requestOfficesWithDurations(DataTable dataTable) {
        forRowDo(dataTable, "times", (times, duration) ->
                forTimesDo(times.intValue(), () -> serviceExecutor.requestOffice(duration)));
    }

    @Then("^the summary of the (employee|office)-requests differs only by (\\d*) %$")
    public void verifySummaryOfEmployeeReuquests(String method, int deviationInPercent, DataTable dataTable) {
        verifySummary(deviationInPercent, dataTable, getMethodName(method));
    }

    @NotNull
    private String getMethodName(String method) {
        return method.equals("employee") ? METHOD_GET_EMPLOYEE : METHOD_GET_OFFICE_OF_EMPLOYEE;
    }

    private void verifySummary(double deviationInPercent, DataTable dataTable, String methodName) {
        String[] metricLines = metricsAccessor.getMetrics();
        forRowDo(dataTable, "quantile", (quantile, duration) -> verifySummaryLine(quantile, duration, deviationInPercent / 100.0, methodName, metricLines));
    }

    private void verifySummaryLine(double quantile, double duration, double deviationInPercent, String methodName, String[] metricLines) {
        String lineKey = metricsAccessor.getMetricsLineKeyForSummary(quantile, methodName);
        Optional<Double> quantileMetricLine = metricsAccessor.getMetricsValue(metricLines, lineKey);
        assertThat(quantileMetricLine).isPresent();
        //noinspection OptionalGetWithoutIsPresent
        assertThat(quantileMetricLine.get()).describedAs("quantile " + quantile).
                isBetween(subtractDeviation(duration, deviationInPercent), addDeviation(duration, deviationInPercent));
    }

    private double subtractDeviation(double duration, double deviationInPercent) {
        return duration * (1.0 - deviationInPercent);
    }

    private double addDeviation(double duration, double deviationInPercent) {
        return duration * (1.0 + deviationInPercent);
    }

    @Then("^the summary count of the (employee|office)-requests is (\\d*)$")
    public void verifySummaryCountOfEmployeeRequests(String method, int expectedCount) {
        assertThat(metricsAccessor.getMetricsValue(METRICS_EXECUTION_SUMMARY_COUNT, getMethodName(method)))
                .hasValue((double) expectedCount);
    }

    @Then("^the summary sum of the (employee|office)-requests is (\\d*.\\d*) with a deviation of (\\d*) %$")
    public void verifySummarySum(String method, double expectedSum, int deviation) {
        Optional<Double> actualSum = metricsAccessor.getMetricsValue(METRICS_EXECUTION_SUMMARY_SUM,
                getMethodName(method));
        assertThat(actualSum).isPresent();
        //noinspection OptionalGetWithoutIsPresent
        assertThat(actualSum.get())
                .isBetween(subtractDeviation(expectedSum, deviation), addDeviation(expectedSum, deviation));
    }

}
