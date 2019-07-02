package at.meks.metrics.demo.jee7;

import cucumber.api.java.After;
import cucumber.api.java.Before;
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
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.lang.Double.parseDouble;
import static org.assertj.core.api.Assertions.assertThat;

public class CucumberSteps {


    private static final String METHOD_GET_EMPLOYEE = "getEmployee";
    private static final String METHOD_GET_OFFICE_OF_EMPLOYEE = "getOfficeOfEmployee";

    private static final String METRICS_EXECUTION_COUNTER = "method_execution_counter";
    private static final String METRICS_EXECUTION_SUMMARY_COUNT = "method_execution_summary_count";
    private static final String METRICS_EXECUTION_SUMMARY_SUM = "method_execution_summary_sum";

    private static final GenericContainer jeeContainer = new GenericContainer("test/demo-jee7:latest")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/jee7").forStatusCode(403).withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES)));

    private RestServiceExecutor webServiceExecutor;
    private MetricsAccessor metricsAccessor;

    @Before
    public void startContainer() {
        if (!jeeContainer.isRunning()) {
            jeeContainer.start();
            webServiceExecutor = new RestServiceExecutor(jeeContainer.getFirstMappedPort());
            metricsAccessor = new MetricsAccessor(webServiceExecutor);
        }
    }

    @After
    public void stopContainer() {
        if (jeeContainer.isRunning()) {
            jeeContainer.stop();
        }
    }

    @When("employees are requested {int} times")
    public void employeesAreRequestedTimes(int times) {
        forTimesDo(times, () -> webServiceExecutor.requestEmployee());
    }

    private void forTimesDo(int times, Runnable runnable) {
        IntStream.range(0, times).forEach(index -> runnable.run());
    }

    @When("offices of employee are request {int} times")
    public void officesOfEmployeeAreRequestTimes(int times) {
        runWithThreads(threadExecutor ->
                forTimesDo(times, () -> threadExecutor.submit(() -> webServiceExecutor.requestOffice())));
    }

    @Then("^the counter of the (employee|office)-requests was increased to (\\d*)$")
    public void verifyCounterValue(String method, int expectedTimes) {
        assertThat(metricsAccessor.getMetricsValue(METRICS_EXECUTION_COUNTER, getMethodName(method)))
                .hasValue((double) expectedTimes);
    }

    @When("employees are requested with following durations")
    public void requestEmployeesWithDurations(DataTable dataTable) {
        runWithThreads(threadExecutor ->
                forRowDo(dataTable, "times", (times, duration) ->
                        forTimesDo(times.intValue(),
                                () -> threadExecutor.submit(() -> webServiceExecutor.requestEmployee(duration)))));
    }

    private void runWithThreads(Consumer<ExecutorService> jobSubmitter) {
        ExecutorService executorService = Executors.newWorkStealingPool(10);
        jobSubmitter.accept(executorService);
        executorService.shutdown();
        try {
            executorService.awaitTermination(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void forRowDo(DataTable dataTable, String leftColumnHeader, BiConsumer<Double, Double> timesDurationConsumer) {
        List<Map<String, String>> durations = dataTable.asMaps();
        durations.stream().sorted((left, right) -> getDuration(left).compareTo(getDuration(right))* -1)
                .forEach(row -> timesDurationConsumer.accept(parseDouble(row.get(leftColumnHeader)),
                    parseDouble(row.get("duration"))));

    }

    private Double getDuration(Map<String, String> row) {
        return parseDouble(row.get("duration"));
    }

    @When("offices of employees are requested with following durations")
    public void requestOfficesWithDurations(DataTable dataTable) {
        runWithThreads(threadExecutor ->
                forRowDo(dataTable, "times", (times, duration) ->
                    forTimesDo(times.intValue(),
                            () -> threadExecutor.submit(() -> webServiceExecutor.requestOffice(duration)))));
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
