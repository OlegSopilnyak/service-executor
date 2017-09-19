package oleg.sopilnyak.exception;

/**
 * Exception throws when call try to execute not exists operation of service
 */
public class OperationNotFoundException extends ServiceCallException{
    private final String operationName;
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     * @param operationName  the name of fail operation
     */
    public OperationNotFoundException(String message, String operationName) {
        super(message);
        this.operationName = operationName;
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
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public OperationNotFoundException(String message, String operationName, Throwable cause) {
        super(message, cause);
        this.operationName = operationName;
    }

    public String getOperationName() {
        return operationName;
    }
}
