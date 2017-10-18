package oleg.sopilnyak.exception;

/**
 * Exception throws during trying to register already registered service
 */
public class ServiceAlreadyRegisteredException extends ServiceRegistrationException{
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public ServiceAlreadyRegisteredException(String message) {
        super(message);
    }
}
