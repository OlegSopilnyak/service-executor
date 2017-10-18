package oleg.sopilnyak.call;

import oleg.sopilnyak.exception.ServiceCallException;

/**
 * Service entity to execute registered operation of service
 */
public interface Call<R, P> {
    /**
     * To get a name of called operation
     *
     * @return the name of operation
     */
    String getOperationName();

    /**
     * To invoke operation of service
     *
     * @param param obligated parameter of operation
     * @param extra extra parameters of operation
     * @return result of operation
     * @throws ServiceCallException throws if cannot execute operation
     */
    R invoke(P param, Object... extra) throws ServiceCallException;

    /**
     * To invoke operation of service without parameters
     *
     * @return result of operation
     * @throws ServiceCallException throws if cannot execute operation
     */
    R invoke() throws ServiceCallException;
}
