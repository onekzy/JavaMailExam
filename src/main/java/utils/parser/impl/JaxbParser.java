package utils.parser.impl;

import org.xml.sax.SAXException;
import utils.parser.Parser;


import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URL;

public class JaxbParser implements Parser {

    ClassLoader classLoader = getClass().getClassLoader();
    InputStream schemaStream = classLoader.getResourceAsStream("schemas/message.xsd");
    StreamSource schemaSource = new StreamSource(schemaStream);

    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema msgSchema = sf.newSchema(schemaSource);

    public JaxbParser() throws SAXException {
    }



    public Object getObject(Reader in, Class c) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(c);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setSchema(msgSchema);
        Object object = unmarshaller.unmarshal(in);

        return object;
    }

    public Object getObject(File in, Class c) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(c);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setSchema(msgSchema);
        Object object = unmarshaller.unmarshal(in);

        return object;
    }

    public void saveObject(File file, Object o) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(o.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setSchema(msgSchema);
        marshaller.marshal(o,file);

    }

    public void saveObject(OutputStream out, Object o) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(o.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setSchema(msgSchema);
        marshaller.marshal(o, out);
    }

    public void saveObject(Writer out, Object o) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(o.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setSchema(msgSchema);
        marshaller.marshal(o, out);
    }

}
