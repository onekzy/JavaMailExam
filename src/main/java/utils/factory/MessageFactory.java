package utils.factory;

import utils.message.impl.MessageXml;

public class MessageFactory {
    public static MessageXml newXmlMessage() {
        return new MessageXml();
    }
}
