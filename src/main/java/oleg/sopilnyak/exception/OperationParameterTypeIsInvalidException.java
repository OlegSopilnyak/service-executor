package oleg.sopilnyak.exception;

/**
 * Exception throws when call try to execute operation of service with wrong type of parameter
 */
public class OperationParameterTypeIsInvalidException extends ServiceCallException{
    private String operationName;
    private int parameterOrder;
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     * @param operationName  the name of fail operation
     * @param parameterOrder order of parameter
     */
    public OperationParameterTypeIsInvalidException(String message, String operationName, int parameterOrder) {
        super(message);
        this.operationName = operationName;
        this.parameterOrder = parameterOrder;
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param operationName  the name of fail operation
     * @param parameterOrder order of parameter
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public OperationParameterTypeIsInvalidException(String message, String operationName, int parameterOrder, Throwable cause) {
        super(message, cause);
        this.operationName = operationName;
        this.parameterOrder = parameterOrder;
    }

    public String getOperationName() {
        return operationName;
    }

    public int getParameterOrder() {
        return parameterOrder;
    }
}
