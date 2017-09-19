package oleg.sopilnyak.builder.impl;

import oleg.sopilnyak.builder.ServiceBuilder;
import oleg.sopilnyak.repository.ServiceMeta;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServiceBuilderImplTest {
    private ServiceBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new ServiceBuilderImpl();
    }

    @Test
    public void id() throws Exception {
        builder.id("1-2-3-4");
        ServiceMeta srv = builder.build();
        assertEquals("1-2-3-4", srv.getId());
    }

    @Test
    public void interfaceClass() throws Exception {
    }

    @Test
    public void operations() throws Exception {
    }

    @Test
    public void build() throws Exception {
    }

    @Test
    public void buildOperation() throws Exception {
    }

}