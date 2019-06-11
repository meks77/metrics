package at.meks.metrics.jee7;

import at.meks.metrics.api.MonitorDurationHistogram;
import io.prometheus.client.Summary;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * The Type Histogram in the metrics standard in microprofile differs from the Histogram in Prometheus.
 * Microprofile's Histogram is equal to Prometheus Summary.
 *
 * Because the implementation is dependend on the the
 * microprofiles standard, the class is named Histogram, while it uses Prometheus Summary.
 */
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@MonitorDurationHistogram
public class MonitorDurationHistogramInterceptor extends AbstractInterceptor {

private static Summary summary;

    static {
        summary = Summary.build().name("method_execution_summary")
                .help("Invocation summary(count, duration) of methods")
                .labelNames(LABEL_NAMES)
                .quantile(0.5, 0.01)
                .quantile(0.75, 0.01)
                .quantile(0.95, 0.01)
                .quantile(0.98, 0.01)
                .quantile(0.99, 0.01)
                .quantile(0.999, 0.01)
                .register();
    }

    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {
        Summary.Timer timer = setLabels(context, summary).startTimer();
        try {
            return context.proceed();
        } finally {
            timer.observeDuration();
        }
    }

}