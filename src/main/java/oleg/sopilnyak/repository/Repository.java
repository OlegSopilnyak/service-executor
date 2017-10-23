package oleg.sopilnyak.repository;

import oleg.sopilnyak.builder.ServiceBuilder;
import oleg.sopilnyak.exception.NoRegisteredServiceException;
import oleg.sopilnyak.exception.ServiceAlreadyRegisteredException;
import oleg.sopilnyak.exception.ServiceCallException;

import java.util.function.Function;

/**
 * Service entity repository to get Call-entity for service remote operation call
 * @see oleg.sopilnyak.call.Call
 */
public interface Repository {

    /**
     * Get implementation of service to invoke operations
     * @param serviceID id of registered service
     * @return registered service operations
     * @throws NoRegisteredServiceException throw if cannot get service-impl
     */
    ServiceImpl getService(String serviceID) throws NoRegisteredServiceException;

    /**
     * Get implementation of service to invoke operations
     * @param serviceInterface parent class of service
     * @return registered service operations
     * @throws NoRegisteredServiceException throw if cannot get service-impl
     */
    ServiceImpl getService(Class serviceInterface) throws NoRegisteredServiceException;

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
