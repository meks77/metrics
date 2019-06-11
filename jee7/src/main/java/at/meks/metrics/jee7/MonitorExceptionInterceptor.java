package at.meks.metrics.jee7;

import at.meks.metrics.api.MonitorException;
import io.prometheus.client.Counter;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@MonitorException
@Priority(Interceptor.Priority.APPLICATION)
public class MonitorExceptionInterceptor extends AbstractInterceptor {

    private static Counter counter;

    static {
        counter = Counter.build().name("method_exception")
                .labelNames(LABEL_NAMES)
                .help("Tracks if exception happens at method").register();
    }

    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {
        try {
            return context.proceed();
        } catch (Exception e) {
            setLabels(context, counter).inc();
            throw e;
        }
    }

}
