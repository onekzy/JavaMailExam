package utils.factory;

import org.xml.sax.SAXException;
import utils.parser.impl.JaxbParser;

public class ParserProvider {
    public static JaxbParser newJaxbParser() throws SAXException {
        return new JaxbParser();
    }
}
