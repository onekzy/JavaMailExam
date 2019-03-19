package client;

import utils.message.impl.Command;

public interface Client {
    void getHumanMsg(Command command);
    void getSystemMsg(Command command);
    Command parseCommand(String raw);
}
