package oleg.sopilnyak.repository.impl;

import net.webservicex.Periodictable;
import net.webservicex.PeriodictableSoap;
import oleg.sopilnyak.builder.OperationBuilder;
import oleg.sopilnyak.builder.ServiceBuilder;
import oleg.sopilnyak.builder.impl.ServiceBuilderImpl;
import oleg.sopilnyak.call.Call;
import oleg.sopilnyak.repository.ServiceMeta;
import oleg.sopilnyak.util.AtomElementService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Integration test for services pool of SOAP web-service,<BR/>
 * test interacts with http://www.webservicex.net/periodictable.asmx
 */
public class ServiceInstancePoolIT {
    private ServiceInstancesPool pool;
//    static {
//        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
//        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
//        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
//    }

    @Before
    public void setUp() throws Exception {
        ServiceMeta meta = makeWebServiceMeta();
        pool = new ServiceInstancesPool(meta, (clazz) -> new Periodictable().getPeriodictableSoap());
    }

    @Test
    public void soapServiceParallelCalls() throws Exception {
        pool.start();

        Call getAtoms = pool.getOperationCall("getAtoms");
        // TODO realize Object operations predefined
        Call toString = pool.getOperationCall("toString");

        Call getAtomicWeight = pool.getOperationCall("getAtomicWeight");
        Call getAtomicNumber = pool.getOperationCall("getAtomicNumber");
        Call getElementSymbol = pool.getOperationCall("getElementSymbol");

        String xmlAtoms = (String) getAtoms.invoke();
        List<String> atoms = AtomElementService.atomElements(xmlAtoms);
//        System.out.println("Service :"+toString.invoke());
        System.out.println("Atom elements " + atoms);
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(10);
        List<CompletableFuture> futures = new ArrayList<>(10);
        try {
            System.out.println(new Date() + " running 30 parallel calls");
            long mark = System.currentTimeMillis();

            for(int i=1;i<=30;i++) {
                CompletableFuture f1 = CompletableFuture.runAsync(() -> {
                    try {
                        String xml = (String) getAtomicWeight.invoke(atoms.get(0));
                        String result = AtomElementService.atomParameter(xml, "AtomicWeight");
                    } catch (Exception e) {
                        System.err.println("getAtomicWeight");
                        e.printStackTrace();
                    }
                }, executorService);
                CompletableFuture f2 = CompletableFuture.runAsync(() -> {
                    try {
                        String xml = (String) getAtomicNumber.invoke(atoms.get(10));
                        String result = AtomElementService.atomParameter(xml, "AtomicNumber");
                    } catch (Exception e) {
                        System.err.println("getAtomicNumber");
                        e.printStackTrace();
                    }
                }, executorService);
                CompletableFuture f3 = CompletableFuture.runAsync(() -> {
                    try {
                        String xml = (String) getElementSymbol.invoke(atoms.get(100));
                        String result = AtomElementService.atomParameter(xml, "Symbol");
                    } catch (Exception e) {
                        System.err.println("getElementSymbol");
                        e.printStackTrace();
                    }
                }, executorService);
                futures.add(CompletableFuture.allOf(f1, f2, f3));
            }


            System.out.println(System.currentTimeMillis() - mark + " msec spend to run");
            mark = System.currentTimeMillis();
            futures.stream().peek(f -> f.join()).collect(Collectors.toList());
            System.out.println(System.currentTimeMillis() - mark + " msec spend to execute.");
        } finally {
            executorService.shutdown();
        }
        assertEquals(0, pool.getInAction().size());
        assertEquals(10, pool.getAvailable().size());
    }

    // private method
    private ServiceMeta makeWebServiceMeta() {
        final ServiceBuilder builder = new ServiceBuilderImpl().interfaceClass(PeriodictableSoap.class);
        final OperationBuilder oBuilder = builder.id("TestWebFacade").operationBuilder();
        builder.operations(
                oBuilder.name("getAtoms").result(String.class).build(),
                oBuilder.name("getAtomicWeight").parameter(String.class).result(String.class).build(),
                oBuilder.name("getAtomicNumber").parameter(String.class).result(String.class).build(),
                oBuilder.name("getElementSymbol").parameter(String.class).result(String.class).build(),
                oBuilder.name("toString").result(String.class).build()
        );
        return builder.build();
    }
}
