package oleg.sopilnyak.builder;

import oleg.sopilnyak.repository.ServiceMeta;

/**
 * Builder service to make service meta-information for register
 */
public interface ServiceBuilder {
    /**
     * Setup id of service
     * @param id value
     * @return builder
     */
    ServiceBuilder id(String id);

    /**
     * Setup interface of remote service
     * @param facade interface of remote services
     * @return builder
     */
    ServiceBuilder interfaceClass(Class facade);

    /**
     * Setup list of operations for this service
     * @param operations array of operations
     * @return builder
     */
    ServiceBuilder operations(ServiceMeta.Operation... operations);

    /**
     * Build service meta-information for service
     * @return service meta
     */
    ServiceMeta build();

    /**
     * Get service operation builder
     * @return operation builder
     */
    OperationBuilder buildOperation();
}
