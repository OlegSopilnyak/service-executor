package oleg.sopilnyak.repository;

import java.lang.reflect.Method;

/**
 * Entity meta-information about service type for describe service immutable
 */
public interface ServiceMeta {
    /**
     * Get id of service (usually it name of interface class)
     * @return service-id
     */
    String getId();

    /**
     * Class - interface of service to call operations
     * @return class
     */
    Class<?> getInterfaceClass();

    /**
     * To get an array of available operations
     * @return array of operation
     */
    Operation[] getOperations();

    // inner classes

    /**
     * Entity - operation, type to describe service operation
     */
    interface Operation{
        /**
         * Operation name
         * @return value
         */
        String getName();

        /**
         * Operation input parameter type
         * @return class of parameter
         */
        Class<?> getParameterClass();

        /**
         * Operation result type
         * @return class of result
         */
        Class<?> getResultClass();

        /**
         * Operation extra parameters types
         * @return extra parameters classes
         */
        Class[] getExtraParameterClasses();

        /**
         * To get Java method to execute operation
         * @return method's instance
         */
        Method getOperationMethod();
    }
}
