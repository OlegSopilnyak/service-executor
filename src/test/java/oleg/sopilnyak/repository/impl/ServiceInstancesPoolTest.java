package oleg.sopilnyak.repository.impl;

import oleg.sopilnyak.builder.OperationBuilder;
import oleg.sopilnyak.builder.ServiceBuilder;
import oleg.sopilnyak.builder.impl.ServiceBuilderImpl;
import oleg.sopilnyak.call.Call;
import oleg.sopilnyak.repository.ServiceMeta;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ServiceInstancesPoolTest {

    private ServiceInstancesPool pool;

    @Before
    public void setUp(){
        ServiceMeta meta = makeServiceMeta();
        pool = new ServiceInstancesPool(meta, facade-> new ServiceFacadeImpl());
    }

    @Test
    public void start() throws Exception {
        assertEquals(false,         pool.isActive());
        assertTrue(pool.getAvailable().isEmpty());
        assertTrue(pool.getInAction().isEmpty());

        pool.setMinimumInstances( 4);

        pool.start();

        assertEquals(true,         pool.isActive());
        assertEquals(4, pool.getAvailable().size());
        assertTrue(pool.getInAction().isEmpty());
    }

    @Test
    public void shutdown() throws Exception {
        assertEquals(false,         pool.isActive());
        pool.shutdown();
        assertEquals(false,         pool.isActive());

        pool.start();
        assertEquals(true,         pool.isActive());

        pool.shutdown();
        assertEquals(false,         pool.isActive());

    }

    @Test
    public void sequentialCall() throws Exception {
        pool.start();

        {
            Call<String, String> serviceCall = pool.<String>getOperationCall("method1");
            String result = serviceCall.invoke("Hi", 123, 23.445, 464, "Bye");
            assertEquals(result, serviceCall.invoke("Hi"));
        }
        {
            Call<Double, Integer> serviceCall = pool.getOperationCall("method2");
            Double result = serviceCall.invoke(100, 101, 102, 103);
            assertEquals(result, serviceCall.invoke(100));
        }
        {
            Call<Object,Object> serviceCall = pool.getOperationCall("method1");
            Object result = serviceCall.invoke();
            assertEquals(result, serviceCall.invoke(100));
        }
    }

    @Test
    public void getMeta() {
        ServiceMeta meta1 = makeServiceMeta();
        ServiceMeta meta2 = pool.getMeta();

        assertNotEquals(meta1, meta2);

        assertEquals(meta1.getId(), meta2.getId());
        assertArrayEquals(meta1.getOperations(), meta2.getOperations());
        assertEquals(meta1.getInterfaceClass(), meta2.getInterfaceClass());
    }

    // private method
    private ServiceMeta makeServiceMeta(){
        ServiceBuilder builder = new ServiceBuilderImpl();
        builder.interfaceClass(ServiceFacade.class).id("TestFacade");
        OperationBuilder oBuilder = builder.operationBuilder();
        ServiceMeta.Operation oper0 = oBuilder.name("method1").result(Void.class).build();
        ServiceMeta.Operation oper1 = oBuilder.name("method1").parameter(String.class).result(String.class).build();
        ServiceMeta.Operation oper2 = oBuilder.name("method2").parameter(Integer.class).result(Double.class).build();
        builder.operations(oper0, oper1, oper2);
       return builder.build();
    }
    // inner classes
    private interface ServiceFacade{
        void method1() throws InterruptedException;
        String method1(String one) throws InterruptedException;
        Double method2(Integer i) throws InterruptedException;
    }
    private static class ServiceFacadeImpl implements ServiceFacade{

        String method1_state = null;

        @Override
        public void method1() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(100);;
        }

        @Override
        public String method1(String one) throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(100);;
            method1_state = one;
            return "State is :"+one;
        }

        @Override
        public Double method2(Integer i) throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(100);;
            return i.doubleValue();
        }
    }
}