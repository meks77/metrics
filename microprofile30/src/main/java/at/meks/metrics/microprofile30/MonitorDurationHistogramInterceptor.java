package at.meks.metrics.microprofile30;

import at.meks.metrics.api.MonitorDurationHistogram;
import org.eclipse.microprofile.metrics.Histogram;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@MonitorDurationHistogram
@Priority(Interceptor.Priority.APPLICATION)
public class MonitorDurationHistogramInterceptor extends AbstractMicroProfileMetricsInterceptor {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {
        Histogram histogram = metricRegistry.histogram("method_execution_duration",
                getClassNameTag(context), getMethodNameTag(context));
        return runWithTimer(context, histogram::update);
    }

}