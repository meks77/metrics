package at.meks.metrics.microprofile30;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.util.function.Consumer;

abstract class AbstractMicroProfileMetricsInterceptor {

    @Inject
    @RegistryType(type= MetricRegistry.Type.APPLICATION)
    protected MetricRegistry metricRegistry;

    Tag getMethodNameTag(InvocationContext context) {
        return new Tag("method", context.getMethod().getName());
    }

    Tag getClassNameTag(InvocationContext context) {
        return new Tag("className", context.getTarget().getClass().getSuperclass().getName());
    }

    Object runWithTimer(InvocationContext context, Consumer<Long> durationConsumer) throws Exception {
        long start = System.currentTimeMillis();
        try {
            return context.proceed();
        } finally {
            durationConsumer.accept(System.currentTimeMillis() - start);
        }
    }
}
