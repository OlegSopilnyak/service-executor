package oleg.sopilnyak.repository;

import oleg.sopilnyak.call.Call;
import oleg.sopilnyak.exception.OperationNotFoundException;

/**
 * Entity to keep operations of service
 */
public interface ServiceImpl {

    /**
     * To get class facade of service
     *
     * @return type of facade
     */
    Class<?> getFacadeClass();

    /**
     * To get entity to invoke operation of service
     *
     * @param name the name of operation
     * @return entity to invoke operation call
     * @throws OperationNotFoundException thrown if cannot get entity
     */
    Call getOperationCall(String name) throws OperationNotFoundException;

    /**
     * To get array of operations execute enititie
     *
     * @return array of calls
     */
    Call[] getCallOperations();

    /**
     * To finish up the work of service
     */
    void shutdown();

    /**
     * To get meat-information about service
     *
     * @return service meta
     */
    ServiceMeta getMeta();
}
