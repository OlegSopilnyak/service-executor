package oleg.sopilnyak.repository;

import oleg.sopilnyak.builder.ServiceBuilder;
import oleg.sopilnyak.call.Call;
import oleg.sopilnyak.exception.NoRegisteredServiceException;
import oleg.sopilnyak.exception.ServiceAlreadyRegisteredException;
import oleg.sopilnyak.exception.ServiceCallException;

import java.util.function.Function;

/**
 * Service entity repository to get Call for service remote operation call
 * @see oleg.sopilnyak.call.Call
 */
public interface Repository {
    /**
     * Prepare call to remote operation
     * @param serviceInterface parent class of service
     * @return call operation entity
     * @throws NoRegisteredServiceException if no registered serviceInterface
     */
    Call prepareCall(Class serviceInterface) throws NoRegisteredServiceException;

    /**
     * Prepare call to remote operation
     * @param serviceID service-id (usually name of service class but not obviously)
     * @return call operation entity
     * @throws NoRegisteredServiceException if no registered service with that ID
     */
    Call prepareCall(String serviceID) throws NoRegisteredServiceException;

    /**
     * To get meta-information about registered service
     * @param serviceInterface parent class of service
     * @return service meta information
     * @throws NoRegisteredServiceException if no registered serviceInterface
     */
    ServiceMeta getRegistered(Class serviceInterface) throws NoRegisteredServiceException;

    /**
     * To get meta-information about registered service
     * @param serviceID service-id (usually name of service class but not obviously)
     * @return service meta information
     * @throws NoRegisteredServiceException if no registered service with that ID
     */
    ServiceMeta getRegistered(String serviceID) throws NoRegisteredServiceException;

    /**
     * Register service in services registry
     * @param metaInfo service meta information
     * @param builder builder of service instance
     * @throws ServiceAlreadyRegisteredException when service already registered
     * @throws ServiceCallException when incorrect information in metaInfo
     */
    void register(ServiceMeta metaInfo, Function builder) throws ServiceAlreadyRegisteredException, ServiceCallException;

    /**
     * To get service builder for registration
     * @return service meta-information builder
     */
    ServiceBuilder serviceBuilder();
}
