package oleg.sopilnyak.builder.impl;

import oleg.sopilnyak.builder.OperationBuilder;
import oleg.sopilnyak.builder.ServiceBuilder;
import oleg.sopilnyak.repository.ServiceMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * Builder builder for service meta
 * @see oleg.sopilnyak.builder.ServiceBuilder
 */
public class ServiceBuilderImpl implements ServiceBuilder{
    private String id;
    private Class facade;
    private final List<ServiceMeta.Operation> operations = new ArrayList<>();
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
        final String id = isEmpty(this.id) ? facade.getClass().getName() : this.id;
        final Class<?> interfaceClass;
        try {
            interfaceClass = facade == null ? Class.forName(id) : facade;
        } catch (ClassNotFoundException e) {
            return null;
        }
        final ServiceMeta.Operation declaredOperations[] = this.operations.isEmpty() ?
                collectInterfaceOperations(interfaceClass)
                : this.operations.toArray(new ServiceMeta.Operation[0]);

        return new ServiceMetaImpl(id, interfaceClass, declaredOperations);
    }

    /**
     * Get service operation builder
     *
     * @return operation builder
     */
    @Override
    public OperationBuilder buildOperation() {
        return new OperationBuilderImpl();
    }

    // private methods

    private static ServiceMeta.Operation[] collectInterfaceOperations(Class<?> interfaceClass) {
        final Method[] methods = interfaceClass.getDeclaredMethods();
        return Stream.of(methods).parallel()
                .map(OperationBuilderImpl::new)
                .map(OperationBuilderImpl::build)
                .collect(Collectors.toList())
                .toArray(new ServiceMeta.Operation[0]);
    }

    // inner classes
    private static class ServiceMetaImpl implements ServiceMeta{
        private final String id;
        private final Class<?> interfaceClass;
        private final Operation[] operations;

        private ServiceMetaImpl(String id, Class<?> interfaceClass, Operation[] operations){
            this.id = id;
            this.interfaceClass = interfaceClass;
            this.operations = operations;
        }
        /**
         * Get id of service (usually it name of interface class)
         *
         * @return service-id
         */
        @Override
        public String getId() {
            return id;
        }

        /**
         * Class - interface of service to call operations
         *
         * @return class
         */
        @Override
        public Class<?> getInterfaceClass() {
            return interfaceClass;
        }

        /**
         * To get an array of available operations
         *
         * @return array of operation
         */
        @Override
        public Operation[] getOperations() {
            return Arrays.copyOf(operations, operations.length);
        }
    }
}
