package oleg.sopilnyak.repository.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sopilnyak.builder.ServiceBuilder;
import oleg.sopilnyak.call.Call;
import oleg.sopilnyak.exception.NoRegisteredServiceException;
import oleg.sopilnyak.exception.ServiceAlreadyRegisteredException;
import oleg.sopilnyak.exception.ServiceCallException;
import oleg.sopilnyak.repository.Repository;
import oleg.sopilnyak.repository.ServiceMeta;

import java.util.function.Function;

/**
 * Realization of services repository
 */
@Slf4j
public class RepositoryImpl implements Repository{
    /**
     * Prepare call to remote operation
     *
     * @param serviceInterface parent class of service
     * @return call operation entity
     * @throws NoRegisteredServiceException if no registered serviceInterface
     */
    @Override
    public Call prepareCall(Class serviceInterface) throws NoRegisteredServiceException {
        return null;
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
        return null;
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

    }

    /**
     * To get service builder for registration
     *
     * @return service meta-information builder
     */
    @Override
    public ServiceBuilder serviceBuilder() {
        return null;
    }
}
