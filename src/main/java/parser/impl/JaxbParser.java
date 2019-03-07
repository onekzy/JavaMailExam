package parser.impl;

import entities.Message;
import org.xml.sax.SAXException;
import parser.Parser;


import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class JaxbParser implements Parser {
    String xsdFile = "src\\main\\resources\\schemas\\message.xsd";
    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema msgSchema = sf.newSchema(new File(xsdFile));

    public JaxbParser() throws SAXException {
    }



    public Object getObject(Reader in, Class c) throws JAXBException {

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
