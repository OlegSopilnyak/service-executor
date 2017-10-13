package oleg.sopilnyak.builder.impl;

import oleg.sopilnyak.builder.OperationBuilder;
import oleg.sopilnyak.repository.ServiceMeta;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class OperationBuilderImplTest {
    private OperationBuilder operationBuilder;

    @Before
    public void setUp() throws Exception {
        operationBuilder = new OperationBuilderImpl();
    }

    @Test
    public void apply() {
        ServiceMeta.Operation fake = fakeOperation(), applied = operationBuilder.apply(fake).build();

        assertEquals(fake.getResultClass(), applied.getResultClass());
        assertArrayEquals(fake.getExtraParameterClasses(), applied.getExtraParameterClasses());
        assertEquals(fake.getName(), applied.getName());
        assertEquals(fake.getParameterClass(), applied.getParameterClass());
        assertEquals(fake.getOperationMethod(), applied.getOperationMethod());
    }

    @Test
    public void name() {
        String name = "12345678";

        assertEquals(name, operationBuilder.name(name).build().getName());
    }

    @Test
    public void parameter() {
        String param = "100";

        assertEquals(String.class, operationBuilder.parameter(param.getClass()).build().getParameterClass());
    }

    @Test
    public void parameterExtra() {
        Integer paramEx = 100;
        assertEquals(Integer.class, operationBuilder.parameterExtra(paramEx.getClass()).build().getExtraParameterClasses()[0]);
    }

    @Test
    public void result() {
        Class resultClass = Void.class;

        assertEquals(Void.class, operationBuilder.result(resultClass).build().getResultClass());
    }

    @Test
    public void method() throws NoSuchMethodException {
        Method toString = Double.class.getDeclaredMethod("toString", new Class[]{});
        ServiceMeta.Operation fake = fakeOperation(), applied = operationBuilder.apply(fake).build();
        operationBuilder.method(toString);

        assertEquals(toString, operationBuilder.build().getOperationMethod());
    }
    // private methods
    private ServiceMeta.Operation fakeOperation(){
        return new ServiceMeta.Operation() {
            @Override
            public String getName() {
                return "fake";
            }

            @Override
            public Class<?> getParameterClass() {
                return String.class;
            }

            @Override
            public Class<?> getResultClass() {
                return Double.class;
            }

            @Override
            public Class[] getExtraParameterClasses() {
                return new Class[0];
            }

            @Override
            public Method getOperationMethod() {
                return null;
            }
        };
    }
}