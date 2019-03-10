package utils.message.impl;

import utils.message.Message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

@XmlRootElement(name = "message")
@XmlType(name = "", propOrder = {"date","from","to","title","subject","body"})
public class MessageXml implements Message {
    private String from;
    private String to;
    private String title;
    private String subject;
    private String body;
    private XMLGregorianCalendar date;

    public String getTitle() {
        return title;
    }

    @XmlElement
    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    @XmlElement
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    @XmlElement
    public void setBody(String body) {
        this.body = body;
    }


    public String getFrom() {
        return from;
    }


    public XMLGregorianCalendar getDate() {
        return date;
    }

    @XmlElement
    public void setDate(XMLGregorianCalendar value) {
        this.date = value;
    }

    @XmlElement
    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    @XmlElement
    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString() {
        String reLine = "From:" + getFrom()+ "\n"
                + "To:" + getTo() + "\n"
                + "Title:" + getTitle() +"\n"
                + "Subject:" + getSubject() + "\n"
                + "Message:" + getBody();
        return reLine;
    }
}
