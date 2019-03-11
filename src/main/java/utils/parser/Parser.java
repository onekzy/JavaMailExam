package utils.parser;


import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.Reader;
import java.io.Writer;

public interface Parser {
    Object getObject(Reader in, Class c) throws JAXBException, SAXException;
    void saveObject(Writer out, Object o) throws JAXBException;
}
