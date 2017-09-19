package oleg.sopilnyak.repository;

import oleg.sopilnyak.call.Call;
import oleg.sopilnyak.exception.NoRegisteredServiceException;

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
}
