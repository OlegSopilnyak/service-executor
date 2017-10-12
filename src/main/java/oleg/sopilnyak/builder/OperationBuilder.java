package oleg.sopilnyak.builder;

import oleg.sopilnyak.repository.ServiceMeta;

import java.lang.reflect.Method;

/**
 * Builder of service meta operations
 */
public interface OperationBuilder {
    OperationBuilder name(String name);
    OperationBuilder parameter(Class paramClass);
    OperationBuilder result(Class resultClass);
    OperationBuilder parameterExtra(Class... paramClass);
    OperationBuilder method(Method method);

    void apply(ServiceMeta.Operation operation);

    ServiceMeta.Operation build();
}
