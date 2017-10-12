package oleg.sopilnyak.builder.impl;

import oleg.sopilnyak.builder.OperationBuilder;
import oleg.sopilnyak.repository.ServiceMeta;

import java.lang.reflect.Method;
import java.util.Arrays;

class OperationBuilderImpl implements OperationBuilder {
    private String name;
    private Class<?> parameterClass;
    private Class<?> returnClass;
    private Class<?>[] extraParameterClasses;
    private Method operationMethod;

    OperationBuilderImpl(ServiceMeta.Operation operation) {
        name = operation.getName();
        parameterClass = operation.getParameterClass();
        extraParameterClasses = operation.getExtraParameterClasses();
        returnClass = operation.getResultClass();
        operationMethod = null;
    }

    OperationBuilderImpl(Method method) {
        assert method != null;
        name = method.getName();
        if (method.getParameterCount() > 0) {
            final Class<?> parameters[] = method.getParameterTypes();
            parameterClass = parameters[0];
            extraParameterClasses = parameters.length > 1 ? Arrays.copyOfRange(parameters, 1, parameters.length):new Class[0];
        }else {
            parameterClass = Void.class;
            extraParameterClasses = new Class[0];
        }
        returnClass = method.getReturnType();
    }

    OperationBuilderImpl() {

    }

    @Override
    public void apply(ServiceMeta.Operation operation) {
        name = operation.getName();
        parameterClass = operation.getParameterClass();
        extraParameterClasses = operation.getExtraParameterClasses();
        returnClass = operation.getResultClass();
        operationMethod = null;
    }


    @Override
    public OperationBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public OperationBuilder parameter(Class paramClass) {
        this.parameterClass = paramClass;
        return this;
    }

    @Override
    public OperationBuilder result(Class resultClass) {
        this.returnClass = resultClass;
        return this;
    }

    @Override
    public OperationBuilder parameterExtra(Class... paramClass) {
        this.extraParameterClasses = paramClass;
        return this;
    }

    @Override
    public OperationBuilder method(Method method) {
        this.operationMethod = method;
        return this;
    }

    @Override
    public ServiceMeta.Operation build() {
        return new OperationImpl(this);
    }
    private static class OperationImpl implements ServiceMeta.Operation{

        private final String name;
        private final Class<?> parameterClass;
        private final Class<?> resultClass;
        private final Class[] extraParameterClasses;
        private final Method operationMethod;

        private OperationImpl(OperationBuilderImpl builder) {
            this.name = builder.name;
            this.parameterClass = builder.parameterClass;
            this.resultClass = builder.returnClass;
            this.extraParameterClasses = builder.extraParameterClasses;
            this.operationMethod = builder.operationMethod;
        }

        /**
         * Operation name
         *
         * @return value
         */
        @Override
        public String getName() {
            return name;
        }

        /**
         * Operation input parameter type
         *
         * @return class of parameter
         */
        @Override
        public Class<?> getParameterClass() {
            return parameterClass;
        }

        /**
         * Operation result type
         *
         * @return class of result
         */
        @Override
        public Class<?> getResultClass() {
            return resultClass;
        }

        /**
         * Operation extra parameters types
         *
         * @return extra parameters classes
         */
        @Override
        public Class[] getExtraParameterClasses() {
            return Arrays.copyOf(extraParameterClasses, extraParameterClasses.length);
        }

        /**
         * To get Java method to execute operation
         *
         * @return method's instance
         */
        @Override
        public Method getOperationMethod() {
            return operationMethod;
        }
    }
}
