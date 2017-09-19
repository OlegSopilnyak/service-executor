package oleg.sopilnyak.builder.impl;

import oleg.sopilnyak.builder.OperationBuilder;
import oleg.sopilnyak.builder.ServiceBuilder;
import oleg.sopilnyak.repository.ServiceMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder builder for service meta
 * @see oleg.sopilnyak.builder.ServiceBuilder
 */
public class ServiceBuilderImpl implements ServiceBuilder{
    private String id;
    private Class facade;
    private List<ServiceMeta.Operation> operations = new ArrayList<>();
    /**
     * Setup id of service
     *
     * @param id value
     * @return builder
     */
    @Override
    public ServiceBuilder id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Setup interface of remote service
     *
     * @param facade interface of remote services
     * @return builder
     */
    @Override
    public ServiceBuilder interfaceClass(Class facade) {
        this.facade = facade;
        return this;
    }

    /**
     * Setup list of operations for this service
     *
     * @param operations array of operations
     * @return builder
     */
    @Override
    public ServiceBuilder operations(ServiceMeta.Operation... operations) {
        this.operations.addAll(Arrays.asList(operations));
        return this;
    }

    /**
     * Build service meta-information for service
     *
     * @return service meta
     */
    @Override
    public ServiceMeta build() {
        return null;
    }

    /**
     * Get service operation builder
     *
     * @return operation builder
     */
    @Override
    public OperationBuilder buildOperation() {
        return null;
    }
}
