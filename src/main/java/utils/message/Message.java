package utils.message;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

public interface Message {
    String getTo();
    String getFrom();
    XMLGregorianCalendar getDate();
}
