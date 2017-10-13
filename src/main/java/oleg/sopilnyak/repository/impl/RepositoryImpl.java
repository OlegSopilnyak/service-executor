package oleg.sopilnyak.repository.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.builder.ServiceBuilder;
import oleg.sopilnyak.builder.impl.ServiceBuilderImpl;
import oleg.sopilnyak.call.Call;
import oleg.sopilnyak.exception.NoRegisteredServiceException;
import oleg.sopilnyak.exception.ServiceAlreadyRegisteredException;
import oleg.sopilnyak.exception.ServiceCallException;
import oleg.sopilnyak.repository.Repository;
import oleg.sopilnyak.repository.ServiceMeta;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Realization of services repository
 */
@Slf4j
public class RepositoryImpl implements Repository{
    private final Lock writePoolLock = new ReentrantLock();
    private final Map<String, ServiceInstancesPool> pools = new ConcurrentHashMap<>();

    @Value("${service.pool.start.instances:2}")
    private int minimumActiveInstances;
    @Value("${service.pool.maximum.instances:10}")
    private int maximumActiveInstances = 10;

    /**
     * Shutdown all service pools
     */
    public void shutdown(){
        pools.values().forEach(ServiceInstancesPool::shutdown);
    }
    /**
     * Prepare call to remote operation
     *
     * @param serviceInterface parent class of service
     * @return call operation entity
     * @throws NoRegisteredServiceException if no registered serviceInterface
     */
    @Override
    public Call prepareCall(Class serviceInterface) throws NoRegisteredServiceException {
        final Optional<ServiceInstancesPool> optional = Optional.ofNullable(pools.get(serviceInterface.getName()));
        return optional
                .orElseThrow(()-> new NoRegisteredServiceException("No registered service of "+serviceInterface))
                .dedicateCall();
    }

    /**
     * Prepare call to remote operation
     *
     * @param serviceID service-id (usually name of service class but not obviously)
     * @return call operation entity
     * @throws NoRegisteredServiceException if no registered service with that ID
     */
    @Override
    public Call prepareCall(String serviceID) throws NoRegisteredServiceException {
        final Optional<ServiceInstancesPool> optional = Optional.ofNullable(pools.get(serviceID));
        return optional
                .orElseThrow(()-> new NoRegisteredServiceException("No registered service with id: "+serviceID))
                .dedicateCall();
    }

    /**
     * To get meta-information about registered service
     *
     * @param serviceInterface parent class of service
     * @return service meta information
     * @throws NoRegisteredServiceException if no registered serviceInterface
     */
    @Override
    public ServiceMeta getRegistered(Class serviceInterface) throws NoRegisteredServiceException {
        return null;
    }

    /**
     * To get meta-information about registered service
     *
     * @param serviceID service-id (usually name of service class but not obviously)
     * @return service meta information
     * @throws NoRegisteredServiceException if no registered service with that ID
     */
    @Override
    public ServiceMeta getRegistered(String serviceID) throws NoRegisteredServiceException {
        return null;
    }

    /**
     * Register service in services registry
     *
     * @param metaInfo service meta information
     * @param builder  builder of service instance
     * @throws ServiceAlreadyRegisteredException when service already registered
     * @throws ServiceCallException              when incorrect information in metaInfo
     */
    @Override
    public void register(ServiceMeta metaInfo, Function builder) throws ServiceAlreadyRegisteredException, ServiceCallException {
        writePoolLock.lock();
        try{
            final String serviceId = metaInfo.getId();

            log.debug("Try to register service with id:{}", serviceId);
            if (pools.containsKey(serviceId)){
                log.error("Service with id:{} already registered.", serviceId);
                throw new ServiceAlreadyRegisteredException("Service with id:"+serviceId+" already registered.");
            }

            log.debug("Try to make instances pool for {}", metaInfo);
            final ServiceInstancesPool pool = makePool(metaInfo, builder);
            pools.put(serviceId, pool);
            // put pool with class-name as id
            final String interfaceId = metaInfo.getInterfaceClass().getName();
            pools.putIfAbsent(interfaceId, pool);

            log.debug("Try to start instances pool for {}", serviceId);
            pool.start();
        }finally {
            writePoolLock.unlock();
        }
    }

    /**
     * To get service builder for registration
     *
     * @return service meta-information builder
     */
    @Override
    public ServiceBuilder serviceBuilder() {
        return new ServiceBuilderImpl();
    }

    // private methods
    private ServiceInstancesPool makePool(ServiceMeta service, Function builder){
        final ServiceInstancesPool pool = new ServiceInstancesPool(service, builder);
        pool.setMinimumInstances(minimumActiveInstances);
        pool.setMaximumInstances(maximumActiveInstances);
        return pool;
    }
}
