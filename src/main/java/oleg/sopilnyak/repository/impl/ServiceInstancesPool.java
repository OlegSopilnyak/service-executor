package oleg.sopilnyak.repository.impl;

import oleg.sopilnyak.call.Call;
import oleg.sopilnyak.exception.OperationNotFoundException;
import oleg.sopilnyak.exception.OperationParameterTypeIsInvalidException;
import oleg.sopilnyak.exception.ServiceCallException;
import oleg.sopilnyak.exception.ServiceExecutionException;
import oleg.sopilnyak.repository.ServiceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Service service-instances pool
 */
class ServiceInstancesPool {
    private static final Logger log = LoggerFactory.getLogger(ServiceInstancesPool.class);
    private String serviceId;
    private Class<?> interfaceClass;
    // map of allowed service operations
    private Map<String, Set<ServiceMeta.Operation>> operations = new HashMap<>();
    private int minimumInstances = 2;
    private int maximumInstances = 10;
    private Function instanceBuilder;
    // set of available instances of service realization
    private Stack available = new Stack();
    // set of busy instances of service realization
    private Set inAction = new LinkedHashSet();
    // Look for buffer
    private final Lock instancesLock = new ReentrantLock();

    /**
     * Prepare call for remote execution
     *
     * @return call instance
     */
    public Call dedicateCall() {
        return new ServiceCall();
    }

    // private methods

    private void freeServiceInstance(Object serviceInstance) {
        instancesLock.lock();
        try{
            if(inAction.remove(serviceInstance)){
                available.push(serviceInstance);
            }else {
                log.error("Strange service instance {}", serviceInstance);
            }
        } finally {
            instancesLock.unlock();
        }
    }

    private Object findFreeInstance() {
        instancesLock.lock();
        try {
            if (available.isEmpty()) {
                if (inAction.size() >= maximumInstances) {
                    log.warn("Services Pool for {} is exhausted.", serviceId);
                    waitForFreeInstances(instancesLock);
                } else {
                    log.debug("Making new service instance for {}", serviceId);
                    final Object serviceInstance = instanceBuilder.apply(interfaceClass);
                    available.push(serviceInstance);
                }
            }
            return returnInActionService();
        } finally {
            instancesLock.unlock();
        }
    }

    private void waitForFreeInstances(Lock instancesLock) {
        while (inAction.size() >= maximumInstances) {
            instancesLock.unlock();
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                log.error("Sleep exception", e);
            }
            instancesLock.lock();
        }
    }

    private Object returnInActionService() {
        final Object service = available.pop();
        inAction.add(service);
        return service;
    }

    // inner classes
    private class ServiceCall implements Call {

        private String name;
        private Object parameter;
        private Object extraParameter;

        /**
         * Setup name of operation
         *
         * @param name name of service operation to execute
         * @return reference to entity
         */
        @Override
        public Call operation(String name) {
            this.name = name;
            return this;
        }

        /**
         * Setup parameter of service method
         *
         * @param param actual parameter
         * @return reference to entity
         */
        @Override
        public Call parameter(Object param) {
            this.parameter = param;
            return this;
        }

        /**
         * Setup extra-parameters of service method
         *
         * @param extra actual parameter
         * @return reference to entity
         */
        @Override
        public Call parameterEx(Object extra) {
            this.extraParameter = extra;
            return this;
        }

        /**
         * Execute remote call and return the result
         *
         * @return result of operation
         * @throws ServiceCallException if something went wrong
         */
        @Override
        public Object execute() throws ServiceCallException {
            log.info("Preparing operation {} for service {}", name, serviceId);
            final ServiceMeta.Operation operation = validateCall();
            final Method operationMethod = operation.getOperationMethod();

            final Object serviceInstance = findFreeInstance();
            final Object[] parameters = makeInvokeParameters();
            try {
                log.info("Executing operation {} for service {}", name, serviceId);
                return operationMethod.invoke(serviceInstance, parameters);
            } catch (Throwable t) {
                final StringBuilder msg = new StringBuilder("Cannot execute operation ")
                        .append(name).append(" of service ").append(serviceId);
                log.error(msg.toString(), t);
                throw new ServiceExecutionException(msg.toString(), t);
            } finally {
                freeServiceInstance(serviceInstance);
            }
        }

        // private methods

        private Object[] makeInvokeParameters() {
            return ObjectUtils.isEmpty(parameter) ? new Object[0]
                    : ObjectUtils.isEmpty(extraParameter) ? new Object[]{parameter} : new Object[]{parameter,extraParameter};
        }

        private ServiceMeta.Operation validateCall() throws ServiceCallException {
            if (StringUtils.isEmpty(name) || !operations.containsKey(name)) {
                throw new OperationNotFoundException("Not found", name);
            }
            for (final ServiceMeta.Operation operation : operations.get(name)) {
                if (isSuit(operation)) {
                    log.debug("Found appropriate operation '{}'", operation);
                    return operation;
                }
            }
            throw new OperationParameterTypeIsInvalidException("Not found", name, 0);
        }

        private boolean isSuit(ServiceMeta.Operation operation) {
            if (ObjectUtils.isEmpty(parameter)) {
                // stop processing parameters
                return operation.getParameterClass().equals(Void.class);
            }
            if (!operation.getParameterClass().isInstance(parameter)) {
                log.debug("Incompatible classes of first operation parameter");
                return false;
            }
            if (ObjectUtils.isEmpty(extraParameter)) {
                // stop processing parameters
                return operation.getExtraParameterClasses().length == 0;
            }
            if (!operation.getExtraParameterClasses()[0].isInstance(extraParameter)) {
                log.debug("Incompatible classes of second operation parameter");
                return false;
            }
            return true;
        }

    }
}
