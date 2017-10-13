package oleg.sopilnyak.builder.impl;

import oleg.sopilnyak.builder.OperationBuilder;
import oleg.sopilnyak.builder.ServiceBuilder;
import oleg.sopilnyak.repository.ServiceMeta;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ServiceBuilderImplTest {
    private ServiceBuilder serviceBuilder;

    @Before
    public void setUp() {
        serviceBuilder = new ServiceBuilderImpl();
    }

    @Test
    public void id(){
        serviceBuilder.id("1-2-3-4").interfaceClass(ServiceBuilder.class);
        ServiceMeta srv = serviceBuilder.build();
        assertEquals("1-2-3-4", srv.getId());
    }

    @Test
    public void interfaceClass(){
        serviceBuilder.id("1-2-3-4").interfaceClass(ServiceBuilder.class);
        ServiceMeta srv = serviceBuilder.build();
        assertEquals(ServiceBuilder.class, srv.getInterfaceClass());
    }

    @Test
    public void operations(){
        serviceBuilder.id("1-2-3-4").interfaceClass(ServiceBuilder.class);

        OperationBuilder oBuilder = serviceBuilder.operationBuilder().name("name1").parameter(String.class).result(Void.class);
        assertNotNull(oBuilder.parameterExtra(Integer.class, Double.class).build());

        serviceBuilder.operations(oBuilder.build(), oBuilder.name("name2").result(Integer.class).build());

        ServiceMeta srv = serviceBuilder.build();

        assertEquals(2,srv.getOperations().length);
        assertEquals("name1", srv.getOperations()[0].getName());
        assertEquals("name2", srv.getOperations()[1].getName());
        assertNotEquals(srv.getOperations()[0].getResultClass(), srv.getOperations()[1].getResultClass());
        assertEquals(Void.class, srv.getOperations()[0].getResultClass());
        assertEquals(Integer.class, srv.getOperations()[1].getResultClass());
    }

}