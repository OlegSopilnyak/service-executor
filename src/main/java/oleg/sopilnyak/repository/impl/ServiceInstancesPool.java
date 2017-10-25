package oleg.sopilnyak.repository.impl;

import oleg.sopilnyak.builder.impl.OperationBuilderImpl;
import oleg.sopilnyak.builder.impl.ServiceBuilderImpl;
import oleg.sopilnyak.call.Call;
import oleg.sopilnyak.exception.*;
import oleg.sopilnyak.repository.ServiceImpl;
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
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Service service-instances pool
 */
public class ServiceInstancesPool implements ServiceMeta, ServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(ServiceInstancesPool.class);
    private volatile boolean active = false;

    private String serviceId;
    private Class<?> interfaceClass;
    // map of allowed service operations
    private Map<String, Set<ServiceMeta.Operation>> operations = new HashMap<>();

    private int minimumInstances = 2;
    private int maximumInstances = 10;
    private Function instanceBuilder;

    // set of available instances of service instances
    private Stack available = new Stack();
    // set of busy instances of service realization
    private Set inAction = new LinkedHashSet();

    // Look for instances containers
    private final Lock instancesLock = new ReentrantLock();

    public ServiceInstancesPool(ServiceMeta service, Function builder) {
        serviceId = service.getId();
        interfaceClass = service.getInterfaceClass();
        instanceBuilder = builder;
        importOperations(service.getOperations());
    }

    /**
     * To start working service instances pool
     *
     * @throws ServiceExecutionException if cannot make appropriate instances
     */
    public void start() throws ServiceExecutionException {
        if (!active) {
            log.info("Starting pool for service-id: {} min: {} max: {}", serviceId, minimumInstances, maximumInstances);
            active = true;
            available.clear();
            inAction.clear();
            try {
                for (int i = 0; i < minimumInstances; i++) {
                    registerServiceInstance();
                }
            } catch (Throwable t) {
                throw new ServiceExecutionException("Cannot start " + serviceId, t);
            }
        }
    }

    /**
     * To stop working with service instances
     */
    public void shutdown() {
        active = false;
    }

    /**
     * To get service meta information
     *
     * @return meta-info of service
     */
    public ServiceMeta getMeta() {
        return new ServiceBuilderImpl(this).build();
    }

    public void setMinimumInstances(int minimumInstances) {
        this.minimumInstances = minimumInstances;
    }

    public void setMaximumInstances(int maximumInstances) {
        this.maximumInstances = maximumInstances;
    }

    /**
     * Get id of service (usually it name of interface class)
     *
     * @return service-id
     */
    @Override
    public String getId() {
        return serviceId;
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
     * To get class facade of service
     *
     * @return type of facade
     */
    @Override
    public Class<?> getFacadeClass() {
        return interfaceClass;
    }

    /**
     * To get entity to invoke operation of service
     *
     * @param name the name of operation
     * @return entity to invoke operation call
     * @throws OperationNotFoundException thrown if cannot get entity
     */
    @Override
    public Call getOperationCall(String name) throws OperationNotFoundException {
        if (operations.get(name) != null) {
            return new OperationCall(name);
        } else throw new OperationNotFoundException("Not registered", name);
    }

    /**
     * To get array of operations execute enititie
     *
     * @return array of calls
     */
    @Override
    public Call[] getCallOperations() {
        return operations.keySet().stream().map(OperationCall::new).collect(toList()).toArray(new Call[0]);
    }

    /**
     * To get an array of available operations
     *
     * @return array of operation
     */
    @Override
    public Operation[] getOperations() {
        return operations.values().stream()
                .flatMap(Collection::stream)
                .map(OperationBuilderImpl::new)
                .map(OperationBuilderImpl::build)
                .collect(toList()).toArray(new Operation[0]);
    }

    // private methods
    private void checkPoolState() {
        if (!active) throw new IllegalStateException("Pool should be active.");
    }

    private void importOperations(Operation[] importOperations) {// TODO realize it at all
        log.debug("Importing {} operations", importOperations.length);
        final Method[] methods = interfaceClass.getDeclaredMethods();
        this.operations.clear();
        Stream.of(importOperations).map(operation -> accept(operation, methods))
                .forEachOrdered(imported -> operations.computeIfAbsent(imported.getName(), (k) -> new LinkedHashSet<>()).add(imported));
    }

    private Operation accept(Operation operation, Method[] methods) {
        final OperationBuilderImpl builder = new OperationBuilderImpl(operation);
        for (Method method : methods) {
            if (isSuit(operation, method)) {
                builder.method(method);
                break;
            }
        }
        return builder.build();
    }

    private static boolean isSuit(Operation operation, Method method) {
        return isSuitNames(operation, method) && isSuitParameters(operation, method) && isSuitResult(operation, method);
    }

    private static boolean isSuitNames(Operation operation, Method method) {
        return method.getName().equals(operation.getName());
    }

    private static boolean isSuitParameters(Operation operation, Method method) {
        final int params = method.getParameterCount();
        return
                params == 0 ? operation.getParameterClass().equals(Void.class)
                        : params == 1 ? operation.getParameterClass().equals(method.getParameterTypes()[0])
                        : operation.getParameterClass().equals(method.getParameterTypes()[0]) &&
                        operation.getExtraParameterClasses().equals(method.getParameterTypes()[1]);
    }

    private static boolean isSuitResult(Operation operation, Method method) {
        return
                operation.getResultClass() == Void.class && "void".equalsIgnoreCase(method.getReturnType().getName()) ||
                        method.getReturnType() == operation.getResultClass();
    }

    private Object executeCall(final OperationCall call, Object param, Object... extra) throws ServiceCallException {
        checkPoolState();
        log.info("Preparing operation {} for service {}", call.name, serviceId);
        Object[] parameters = makeInvokeParameters(param, extra);
        final Method operationMethod = call.getValidOperation(parameters).getOperationMethod();

        final Object serviceInstance = findFreeInstance();
        log.debug("Found free service instance {}", serviceInstance);

        try {
            log.debug("Executing operation {} for service {}", call.name, serviceId);
            final int methodParametersCount = operationMethod.getParameterCount();
            if (methodParametersCount < parameters.length){
                log.warn("Reject last {} actual parameters for operation .{}. of service '{}'", parameters.length-methodParametersCount, call.getOperationName(), serviceId);
                parameters = Arrays.copyOf(parameters, methodParametersCount);
            }
            // execute operation method
            final Object result = operationMethod.invoke(serviceInstance, parameters);
            // return execution result
            log.debug("Execution result operation {} for service {} = '{}'", call.name, serviceId, result);
            return result;
        } catch (Throwable t) {
            // something went wrong
            final StringBuilder msg = new StringBuilder("Cannot execute operation ")
                    .append(call.name).append(" of service ").append(serviceId);
            log.error(msg.toString(), t);
            throw new ServiceExecutionException(msg.toString(), t);
        } finally {
            log.debug("Return to free service instances {}", serviceInstance);
            freeServiceInstance(serviceInstance);
        }
    }

    private static Object[] makeInvokeParameters(Object param, Object[] extra) {
        if (ObjectUtils.isEmpty(param)) return new Object[0];
        final List params = new ArrayList(Collections.singletonList(param));
        if (!ObjectUtils.isEmpty(extra)) params.addAll(asList(extra));
        return params.toArray();
    }

    private Object findFreeInstance() throws ServiceCallException {
        instancesLock.lock();
        try {
            if (available.isEmpty()) {
                if (inAction.size() >= maximumInstances) {
                    log.warn("Services Pool for {} is exhausted.", serviceId);
                    waitForFreeInstances(instancesLock);
                } else {
                    registerServiceInstance();
                }
            }
            return returnInActionService();
        } catch (InvalidServiceMetaInformation e) {
            log.error("Wrong service-meta ", e);
            shutdown();
            throw new ServiceCallException("Pool is not valid", e);
        } finally {
            instancesLock.unlock();
        }
    }

    private void registerServiceInstance() throws InvalidServiceMetaInformation {
        log.debug("Making new service instance for Id: {} / Facade: '{}'", serviceId, interfaceClass);
        final Object serviceInstance = instanceBuilder.apply(interfaceClass);
        if (interfaceClass.isInstance(serviceInstance)) {
            available.push(serviceInstance);
        } else {
            throw new InvalidServiceMetaInformation("Incompatible facade interface and built realization.");
        }
    }

    private void waitForFreeInstances(Lock instancesLock) {
        log.debug("******* Waiting for free instance");
        while (inAction.size() >= maximumInstances) {
            instancesLock.unlock();
            try {
                TimeUnit.MILLISECONDS.sleep(500);
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

    private void freeServiceInstance(Object serviceInstance) {
        instancesLock.lock();
        try {
            if (inAction.remove(serviceInstance) && active) {
                available.push(serviceInstance);
            } else {
                log.error("Strange service instance {}", serviceInstance);
            }
        } finally {
            instancesLock.unlock();
        }
    }

    /**
     * For test purposes only
     *
     * @return available instances stack
     */
    Stack getAvailable() {
        return available;
    }

    /**
     * For test purposes only
     *
     * @return in-action instances set
     */
    Set getInAction() {
        return inAction;
    }

    /**
     * For test purposes only
     *
     * @return true if active
     */
    boolean isActive() {
        return active;
    }

    // inner classes
    private class OperationCall<R> implements Call<R, Object> {
        private final String name;

        public OperationCall(String name) {
            this.name = name;
        }

        /**
         * To get a name of called operation
         *
         * @return the name of operation
         */
        @Override
        public String getOperationName() {
            return name;
        }

        /**
         * To invoke operation of service
         *
         * @param param obligated parameter of operation
         * @param extra extra parameters of operation
         * @return result of operation
         * @throws ServiceCallException throws if cannot execute operation
         */
        @Override
        public R invoke(Object param, Object... extra) throws ServiceCallException {
            return (R) executeCall(this, param, extra);
        }

        /**
         * To invoke operation of service without parameters
         *
         * @return result of operation
         * @throws ServiceCallException throws if cannot execute operation
         */
        @Override
        public R invoke() throws ServiceCallException {
            return (R) executeCall(this, null);
        }

        // private methods
        private ServiceMeta.Operation getValidOperation(Object[] params) throws ServiceCallException {
            if (StringUtils.isEmpty(name) || !operations.containsKey(name)) {
                throw new OperationNotFoundException("Not found", name);
            }
            for (final ServiceMeta.Operation operation : operations.get(name)) {
                if (isSuit(operation, params)) {
                    log.debug("Found appropriate operation '{}'", operation);
                    return operation;
                }
            }
            throw new OperationParameterTypeIsInvalidException("Not found", name, 0);
        }

        private boolean isSuit(ServiceMeta.Operation operation, Object[] params) {
            if (ObjectUtils.isEmpty(params) || operation.getParameterClass() == Void.class) {
                // stop processing parameters
                log.debug("No actual parameters in call operation '{}'", name);
                return operation.getParameterClass() == Void.class;
            }
            final Object obligateParameter = params[0];
            if (!operation.getParameterClass().isInstance(obligateParameter)) {
                log.debug("Incompatible classes of first operation parameter");
                return false;
            }
            if (params.length == 1) {
                log.debug("Only one actual parameter for operation '{}' ({})", name, obligateParameter);
                return true;
            }
            final Object extraParameter = params[1];
            if (ObjectUtils.isEmpty(extraParameter)) {
                // stop processing parameters
                return operation.getExtraParameterClasses().length == 0;
            }
            if (!ObjectUtils.isEmpty(operation.getExtraParameterClasses()) && !operation.getExtraParameterClasses()[0].isInstance(extraParameter)) {
                log.debug("Incompatible classes of second operation parameter");
                return false;
            }
            log.debug("Passed two actual parameter for operation '{}' ({}, {})", name, obligateParameter, extraParameter);
            return true;
        }

    }

}

