package oleg.sopilnyak.builder.impl;

import oleg.sopilnyak.builder.OperationBuilder;
import oleg.sopilnyak.repository.ServiceMeta;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class OperationBuilderImpl implements OperationBuilder {
    private String name;
    private Class<?> parameterClass = Void.class;
    private Class<?> returnClass = Void.class;
    private Class<?>[] extraParameterClasses = new Class[0];
    private Method operationMethod;

    public OperationBuilderImpl(ServiceMeta.Operation operation) {
        assert operation != null;
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
    public OperationBuilder apply(ServiceMeta.Operation operation) {
        name = operation.getName();
        parameterClass = operation.getParameterClass();
        extraParameterClasses = operation.getExtraParameterClasses();
        returnClass = operation.getResultClass();
        operationMethod = null;
        return this;
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

    // inner classes
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OperationImpl operation = (OperationImpl) o;
            return Objects.equals(name, operation.name) &&
                    Objects.equals(parameterClass, operation.parameterClass) &&
                    Objects.equals(resultClass, operation.resultClass) &&
                    Arrays.equals(extraParameterClasses, operation.extraParameterClasses);
        }

        @Override
        public int hashCode() {

            int result = Objects.hash(name, parameterClass, resultClass);
            result = 31 * result + Arrays.hashCode(extraParameterClasses);
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("OperationMeta{");
            sb.append("name='").append(name).append('\'');
            sb.append(", parameterClass=").append(parameterClass);
            sb.append(", resultClass=").append(resultClass);
            sb.append(", extraParameterClasses=").append(Arrays.toString(extraParameterClasses));
            sb.append('}');
            return sb.toString();
        }
    }
}
