package at.meks.metrics.demo.jee7;

import cucumber.api.java.After;
import cucumber.api.java.Before;
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
import static java.lang.Integer.parseInt;
import static org.assertj.core.api.Assertions.assertThat;

public class CucumberSteps {


    private static final String METHOD_GET_EMPLOYEE = "getEmployee";
    private static final String METHOD_GET_OFFICE_OF_EMPLOYEE = "getOfficeOfEmployee";

    private static final String METRICS_EXECUTION_COUNTER = "method_execution_counter";

    private static final String METRICS_EXECUTION_SUMMARY_COUNT = "method_execution_summary_count";
    private static final String METRICS_EXECUTION_SUMMARY_SUM = "method_execution_summary_sum";

    private static final String METRIC_EXECUTION_HISTOGRAM_COUNT = "method_execution_histogram_count";
    private static final String METRICS_EXECUTION_HISTOGRAM_SUM = "method_execution_histogram_sum";

    private static final String METRIC_METHOD_EXCEPTION = "method_exception";

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

    @When("^employees are requested (\\d*) times( with error)?$")
    public void employeesAreRequestedTimes(int times, String withError) {
        boolean requestWithError = " with error".equals(withError);
        forTimesDo(times, () -> webServiceExecutor.requestEmployee(requestWithError));
    }

    private void forTimesDo(int times, Runnable runnable) {
        IntStream.range(0, times).forEach(index -> runnable.run());
    }

    @When("^offices of employee are request (\\d*) times( with error)?$")
    public void officesOfEmployeeAreRequestTimes(int times, String withError) {
        boolean requestWithError = " with error".equals(withError);
        runWithThreads(threadExecutor ->
                forTimesDo(times, () -> threadExecutor.submit(() -> webServiceExecutor.requestOffice(requestWithError))));
    }

    @Then("^the (exception )?counter of the (employee|office)-requests was increased to (\\d*)$")
    public void verifyCounterValue(String exceptionCounter, String method, int expectedTimes) {
        String metricName = getMetricCounterName(exceptionCounter);
        assertThat(metricsAccessor.getMetricsValue(metricName, getMethodName(method)))
                .hasValue((double) expectedTimes);
    }

    @NotNull
    private String getMetricCounterName(String exceptionCounter) {
        String metricName;
        if ("exception ".equals(exceptionCounter)) {
            metricName = METRIC_METHOD_EXCEPTION;
        } else {
            metricName = METRICS_EXECUTION_COUNTER;
        }
        return metricName;
    }

    @When("employees are requested with following durations")
    public void requestEmployeesWithDurations(DataTable dataTable) {
        runWithThreads(threadExecutor ->
                forRowDo(dataTable, "times", "duration", (times, duration) ->
                        forTimesDo(parseInt(times),
                                () -> threadExecutor.submit(() -> webServiceExecutor.requestEmployee(duration, false)))));
    }

    private void runWithThreads(Consumer<ExecutorService> jobSubmitter) {
        ExecutorService executorService = Executors.newWorkStealingPool(2);
        jobSubmitter.accept(executorService);
        executorService.shutdown();
        try {
            executorService.awaitTermination(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void forRowDo(DataTable dataTable, String keyColumnHeader, String valueColumnHeader,
            BiConsumer<String, Double> timesDurationConsumer) {
        List<Map<String, String>> durations = dataTable.asMaps();
        durations.forEach(row -> timesDurationConsumer.accept(row.get(keyColumnHeader),
                    parseDouble(row.get(valueColumnHeader))));
    }

    private Double getDuration(Map<String, String> row) {
        return parseDouble(row.get("duration"));
    }

    @When("offices of employees are requested with following durations")
    public void requestOfficesWithDurations(DataTable dataTable) {
        runWithThreads(threadExecutor ->
                forRowDo(dataTable, "times", "duration", (times, duration) ->
                    forTimesDo(parseInt(times),
                            () -> threadExecutor.submit(() -> webServiceExecutor.requestOffice(duration, false)))));
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
        forRowDo(dataTable, "quantile", "duration", (quantile, duration) ->
                verifySummaryLine(quantile, duration, deviationInPercent / 100.0, methodName, metricLines));
    }

    private void verifySummaryLine(String quantile, double duration, double deviationInPercent, String methodName, String[] metricLines) {
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

    @Then("^the (summary|histogram) count of the (employee|office)-requests is (\\d*)$")
    public void verifyAdditionalMetricsCountOfEmployeeRequests(String summaryOrHistorgram, String method, int expectedCount) {
        String metricName;
        if ("summary".equals(summaryOrHistorgram)) {
            metricName = METRICS_EXECUTION_SUMMARY_COUNT;
        } else {
            metricName = METRIC_EXECUTION_HISTOGRAM_COUNT;
        }
        assertThat(metricsAccessor.getMetricsValue(metricName, getMethodName(method)))
                .hasValue((double) expectedCount);
    }

    @Then("^the (summary|histogram) sum of the (employee|office)-requests is (\\d*.\\d*) with a deviation of (\\d*) %$")
    public void verifySummarySum(String summaryOrHistogram, String method, double expectedSum, int deviation) {
        String metricName;
        if ("summary".equals(summaryOrHistogram)) {
            metricName = METRICS_EXECUTION_SUMMARY_SUM;
        } else {
            metricName = METRICS_EXECUTION_HISTOGRAM_SUM;
        }
        Optional<Double> actualSum = metricsAccessor.getMetricsValue(metricName, getMethodName(method));
        assertThat(actualSum).isPresent();
        //noinspection OptionalGetWithoutIsPresent
        assertThat(actualSum.get())
                .isBetween(subtractDeviation(expectedSum, deviation), addDeviation(expectedSum, deviation));
    }

    @Then("^the histogram of the (employee|office)-requests differs only by (\\d*) %$")
    public void verifyHistogramWithDeviation(String method, int deviationInPercent, DataTable dataTable) {
        String[] metricLines = metricsAccessor.getMetrics();
        forRowDo(dataTable, "duration less equals", "count", (bucket, count) ->
                verifyHisogramLine(bucket, count.intValue(), deviationInPercent / 100.0, getMethodName(method), metricLines));
    }

    private void verifyHisogramLine(String bucket, int count, double deviation, String methodName, String[] metricLines) {
        String lineKey = metricsAccessor.getMetricsLineKeyForHistogram(bucket, methodName);
        Optional<Double> durationMetricLine = metricsAccessor.getMetricsValue(metricLines, lineKey);
        assertThat(durationMetricLine).isPresent();
        //noinspection OptionalGetWithoutIsPresent
        assertThat(durationMetricLine.get()).describedAs("bucket " + bucket).
                isBetween(Math.floor(subtractDeviation(count, deviation)), Math.ceil(addDeviation(count, deviation)));
    }

}
