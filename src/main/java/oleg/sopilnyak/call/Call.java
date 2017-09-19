package oleg.sopilnyak.call;

import oleg.sopilnyak.exception.ServiceCallException;

/**
 * Service entity to execute registered service call
 */
public interface Call<R, P> {
    /**
     * Setup name of operation
     * @param name name of service operation to execute
     * @return reference to entity
     */
    Call operation(String name);

    /**
     * Setup parameter of service method
     * @param param actual parameter
     * @return reference to entity
     */
    Call parameter(P param);

    /**
     * Setup extra-parameters of service method
     * @param extra actual parameter
     * @return reference to entity
     */
    Call parameterEx(Object extra);

    /**
     * Execute remote call and return the result
     * @return result of operation
     * @throws ServiceCallException if something went wrong
     */
    R execute() throws ServiceCallException;
}
