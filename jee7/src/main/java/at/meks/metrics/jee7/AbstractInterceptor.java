package at.meks.metrics.jee7;

import io.prometheus.client.SimpleCollector;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

abstract class AbstractInterceptor {

    static final String[] LABEL_NAMES = {"class_name", "method_name", "method_args"};

    <T, C extends SimpleCollector<T>> T setLabels(InvocationContext context, C collector) {
        return collector.labels(getClassName(context), context.getMethod().getName(),
                Arrays.stream(context.getMethod().getGenericParameterTypes())
                        .map(Type::getTypeName)
                        .collect(Collectors.joining()));
    }

    private String getClassName(InvocationContext context) {
        // the target class is the proxy class. to get the real class name the super class name must be used
        return context.getTarget().getClass().getSuperclass().getName();
    }
}
