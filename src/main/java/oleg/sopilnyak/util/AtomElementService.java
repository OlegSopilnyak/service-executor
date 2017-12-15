package oleg.sopilnyak.util;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to extract responses from Periodical SOAP Service
 */
public class AtomElementService {
    private final static SAXBuilder builder = new SAXBuilder();

    public static List<String> atomElements(String atoms) throws JDOMException, IOException {
        final List<String> result = new ArrayList<>();
        final Element dataSet = builder.build(new StringReader(atoms)).getRootElement();
        for (final Element table : dataSet.getChildren("Table")) {
            String atom = table.getChildTextTrim("ElementName");
            result.add(atom);
        }
        return result;
    }
    public static  String atomParameter(String xml, String parameter) throws JDOMException, IOException {
        final Element dataSet = builder.build(new StringReader(xml)).getRootElement();
        final Element table = dataSet.getChild("Table");
        return  table.getChildTextTrim(parameter);
    }
}
