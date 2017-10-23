package oleg.sopilnyak.repository.impl;

import net.webservicex.Periodictable;
import net.webservicex.PeriodictableSoap;
import org.junit.Test;

/**
 * Test case for integration test
 */
public class ServiceInstancePoolIT {

    private PeriodictableSoap service = new Periodictable().getPeriodictableSoap();

    @Test
    public void testSoapService() {
        String atoms = service.getAtoms();
        System.out.println("Atoms :" + atoms);
    }
}
