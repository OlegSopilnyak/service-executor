package oleg.sopilnyak.repository.impl;

import oleg.sopilnyak.builder.OperationBuilder;
import oleg.sopilnyak.builder.ServiceBuilder;
import oleg.sopilnyak.builder.impl.ServiceBuilderImpl;
import oleg.sopilnyak.call.Call;
import oleg.sopilnyak.exception.ServiceCallException;
import oleg.sopilnyak.repository.ServiceMeta;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ServiceInstancesPoolTest {

    private ServiceInstancesPool pool;

    private static Object apply(Object facade) {
        return new ServiceFacadeImpl();
    }

    @Before
    public void setUp(){
        ServiceMeta meta = makeServiceMeta();
        pool = new ServiceInstancesPool(meta, ServiceInstancesPoolTest::apply);
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
    public void simpleServiceSequentialCalls() throws Exception {
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
    public void simpleServiceParallelCalls() throws Exception {
        pool.start();
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(10);
        List<CompletableFuture> futures = new ArrayList<>(60);
        Call stringMethod1 = pool.getOperationCall("method1");
        Call doubleMethod2 = pool.getOperationCall("method2");
        Call voidMethod1 = pool.getOperationCall("method1");
        try{
            System.out.println(new Date()+" running 60 parallel calls");
            long mark = System.currentTimeMillis();
            for(int i=1;i<=60;i++) {
                CompletableFuture f1 = CompletableFuture.runAsync(() -> {
                    try {
                        stringMethod1.invoke("Hi");
                    } catch (ServiceCallException e) {
                        System.err.println("stringMethod1");
                        e.printStackTrace();
                    }
                }, executorService);
                CompletableFuture f2 = CompletableFuture.runAsync(() -> {
                    try {
                        doubleMethod2.invoke(2000);
                    } catch (ServiceCallException e) {
                        System.err.println("doubleMethod2");
                        e.printStackTrace();
                    }
                }, executorService);
                CompletableFuture f3 = CompletableFuture.runAsync(() -> {
                    try {
                        voidMethod1.invoke();
                    } catch (ServiceCallException e) {
                        System.err.println("voidMethod1");
                        e.printStackTrace();
                    }
                }, executorService);
                futures.add(CompletableFuture.allOf(f1, f2, f3));
            }
            System.out.println(System.currentTimeMillis()-mark + " msec spend to run");
            mark = System.currentTimeMillis();
            futures.stream().peek(f-> f.join()).collect(Collectors.toList());
            System.out.println(System.currentTimeMillis()-mark + " msec spend to execute.");
        }finally {
            executorService.shutdown();
        }
        assertEquals(0, pool.getInAction().size());
        assertEquals(10, pool.getAvailable().size());
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
            TimeUnit.MILLISECONDS.sleep(100);
        }

        @Override
        public String method1(String one) throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(100);
            method1_state = one;
            return "State is :"+one;
        }

        @Override
        public Double method2(Integer i) throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(100);
            return i.doubleValue();
        }
    }
}