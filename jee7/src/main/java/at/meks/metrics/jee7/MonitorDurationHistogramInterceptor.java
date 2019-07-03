package at.meks.metrics.jee7;

import at.meks.metrics.api.MonitorDurationHistogram;
import io.prometheus.client.Histogram;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@MonitorDurationHistogram
public class MonitorDurationHistogramInterceptor extends AbstractInterceptor {

private static Histogram histogram;

    static {
        histogram = Histogram.build().name("method_execution_histogram")
                .help("Invocation histogram(count, duration) of methods")
                .labelNames(LABEL_NAMES)
                .register();
    }

    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {
        Histogram.Timer timer = setLabels(context, histogram).startTimer();
        try {
            return context.proceed();
        } finally {
            timer.observeDuration();
        }
    }

}