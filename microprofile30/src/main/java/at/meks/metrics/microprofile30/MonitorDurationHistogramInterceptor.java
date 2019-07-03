package at.meks.metrics.microprofile30;

import at.meks.metrics.api.MonitorDurationHistogram;
import at.meks.metrics.api.MonitorDurationHistogramSummary;
import org.eclipse.microprofile.metrics.Histogram;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * because in the Micorprofile 3.0 is still no histogram available like in prometheus, only the summary of the histogram
 * is supported for both annotations
 */
@Interceptor
@MonitorDurationHistogram
@MonitorDurationHistogramSummary
@Priority(Interceptor.Priority.APPLICATION)
public class MonitorDurationHistogramInterceptor extends AbstractMicroProfileMetricsInterceptor {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {
        Histogram histogram = metricRegistry.histogram("method_execution_duration",
                getClassNameTag(context), getMethodNameTag(context));
        return runWithTimer(context, histogram::update);
    }

}