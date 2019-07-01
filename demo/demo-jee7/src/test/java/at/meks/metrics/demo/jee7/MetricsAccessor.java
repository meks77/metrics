package at.meks.metrics.demo.jee7;

import io.restassured.RestAssured;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

class MetricsAccessor {

    private static final String METRICS_URL = "jee7/metrics";

    private final RestServiceExecutor serviceExecutor;

    MetricsAccessor(RestServiceExecutor serviceExecutor) {
        this.serviceExecutor = serviceExecutor;
    }

    @NotNull
    Optional<Double> getMetricsValue(String[] metricLines, String quantileMetricLineWithoutValue) {
        return Arrays.stream(metricLines)
                .filter(line -> line.startsWith(quantileMetricLineWithoutValue))
                .map(line -> Double.parseDouble(line.substring(quantileMetricLineWithoutValue.length()-1).trim()))
                .findFirst();
    }

    @NotNull
    Optional<Double> getMetricsValue(String metricsName, String methodName) {
        String metricsLineKey = getMetricsLineKey(metricsName, methodName, null);
        return Arrays.stream(getMetrics())
                .filter(line -> line.startsWith(metricsLineKey))
                .map(line -> Double.parseDouble(line.substring(metricsLineKey.length()-1).trim()))
                .findFirst();
    }

    @NotNull
    private String getMetricsLineKey(String metricName, String methodName, Supplier<String> additionalKeyString) {
        String key = metricName + "{" +
                "class_name=\"at.meks.metrics.demo.application.EmployeeService\"," +
                "method_name=\"" + methodName + "\"," +
                "method_args=\"java.lang.Stringint\",";
        if (additionalKeyString != null) {
            key += additionalKeyString.get() + ",";
        }
        key += "} ";
        return key;
    }

    @NotNull String[] getMetrics() {
        String response = RestAssured.get(serviceExecutor.getUrl(METRICS_URL)).body().asString();
        return response.split("\n");
    }

    String getMetricsLineKeyForSummary(double quantile, String methodName) {
        return getMetricsLineKey("method_execution_summary", methodName, () -> "quantile=\"" + quantile + "\"");
    }
}
