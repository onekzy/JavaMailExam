package server;

import server.serverEntities.Connection;

public interface Server {
    void removeClient(Connection client);
    void addRegUser(String login, String password);
    boolean checkReg(String login);
    boolean getAuthority(String login, String password);
    String getCommands();
}
