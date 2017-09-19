package oleg.sopilnyak.builder;

import oleg.sopilnyak.repository.ServiceMeta;

/**
 * Builder of service meta operations
 */
public interface OperationBuilder {
    OperationBuilder name(String name);
    OperationBuilder parameter(Class paramClass);
    OperationBuilder result(Class resultClass);
    OperationBuilder parameterExtra(Class... paramClass);
    ServiceMeta.Operation build();
}
