
package utils.message.impl;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the schemas package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Command_QNAME = new QName("", "command");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: schemas
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Command }
     * 
     */
    public Command createCommand() {
        return new Command();
    }

    /**
     * Create an instance of {@link Details }
     * 
     */
    public Details createDetails() {
        return new Details();
    }

    /**
     * Create an instance of {@link MessageXml }
     * 
     */
    public MessageXml createMessage() {
        return new MessageXml();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Command }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "command")
    public JAXBElement<Command> createCommand(Command value) {
        return new JAXBElement<Command>(_Command_QNAME, Command.class, null, value);
    }

}
