package at.meks.metrics.jee7;

import at.meks.metrics.api.MonitorExecutionCount;
import io.prometheus.client.Counter;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@MonitorExecutionCount
@Priority(Interceptor.Priority.APPLICATION)
public class MonitorExecutionCountInterceptor extends AbstractInterceptor {

    private static Counter counter;

    static {
        counter = Counter.build().name("method_execution_counter")
                .labelNames(LABEL_NAMES)
                .help("Invocation counts of methods").register();
    }

    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {
        setLabels(context, counter).inc();
        return context.proceed();
    }

}
