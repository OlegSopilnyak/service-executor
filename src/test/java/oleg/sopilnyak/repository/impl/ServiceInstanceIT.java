package oleg.sopilnyak.repository.impl;

import net.webservicex.Periodictable;
import net.webservicex.PeriodictableSoap;
import oleg.sopilnyak.util.AtomElementService;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test case for integration test
 */
public class ServiceInstanceIT {

//    static {
//        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
//        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
//        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
//    }
    private final static PeriodictableSoap service = new Periodictable().getPeriodictableSoap();

    @After
    public void tearDown() throws Exception {
        TimeUnit.MILLISECONDS.sleep(20);
    }

    @Test
    public void testSoapServiceGetAtoms() throws JDOMException, IOException {
        String atoms = service.getAtoms();
        List<String> elements = AtomElementService.atomElements(atoms);
        assertNotNull(elements);
        assertTrue(elements.size() > 0);
        System.out.println("Atoms :"+elements);
    }
    @Test
    public void testSoapServiceGetWeight() throws JDOMException, IOException {
        String atoms = service.getAtoms();
        List<String> elements = AtomElementService.atomElements(atoms);
        assertTrue(elements.size() > 0);

        String result = service.getAtomicWeight(elements.get(10));
        result = AtomElementService.atomParameter(result, "AtomicWeight");
        assertNotNull(result);
        System.out.println("Atom :"+elements.get(10)+" weight :"+result);
    }
    @Test
    public void testSoapServiceGetNumber() throws JDOMException, IOException {
        String atoms = service.getAtoms();
        List<String> elements = AtomElementService.atomElements(atoms);
        assertTrue(elements.size() > 0);

        String result = service.getAtomicNumber(elements.get(20));
        result = AtomElementService.atomParameter(result, "AtomicNumber");
        assertNotNull(result);
        System.out.println("Atom :"+elements.get(20)+" number :"+result);
    }
    @Test
    public void testSoapServiceGetSymbol() throws JDOMException, IOException {
        String atoms = service.getAtoms();
        List<String> elements = AtomElementService.atomElements(atoms);
        assertTrue(elements.size() > 0);

        String result = service.getElementSymbol(elements.get(30));
        result = AtomElementService.atomParameter(result, "Symbol");
        assertNotNull(result);
        System.out.println("Atom :"+elements.get(30)+" symbol :"+result);
    }
}
