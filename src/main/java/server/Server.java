package server;

import server.serverEntities.Connection;
import utils.message.impl.MessageXml;

public interface Server {
    void removeClient(Connection client);
    void addRegUser(String login, String password);
    boolean checkReg(String login);
    boolean getAuthority(String login, String password);
    String getCommands();
}
