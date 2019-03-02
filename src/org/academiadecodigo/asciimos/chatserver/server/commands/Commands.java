package org.academiadecodigo.asciimos.chatserver.server.commands;

import org.academiadecodigo.asciimos.chatserver.server.ClientHandler;

import java.io.IOException;

public enum Commands {
    QUIT() {
        @Override
        public void execute(ClientHandler clientHandler, String arg) throws IOException {
            clientHandler.sendMessage("Bye Bye!");
            clientHandler.closeSocket();
        }
    },
    HELP() {
        @Override
        public void execute(ClientHandler clientHandler, String arg) throws IOException {
            clientHandler.sendMessage("----- Command Helper -----\n" +
                    "1. /setname <name>\n" +
                    "2. /quit <reason>\n" +
                    "--------------------");
        }
    },
    SETNAME() {
        @Override
        public void execute(ClientHandler clientHandler, String arg) throws IOException {
            String[] args = arg.split(" ");
            if(args.length <= 1) {
                clientHandler.sendMessage("[SERVER] You need to specify your nickname. Eg: /setname <name>");
                return;
            }
            clientHandler.setName(args[1]);
            clientHandler.sendMessage("[SERVER] Your nickname has been changed to " + args[1]);
        }
    };

    public static Commands isCommand(String data, ClientHandler clientHandler) throws IOException {
        String[] args = data.split(" ");
        for (Commands command : values()) {
            if (command.toString().equals(args[0].toUpperCase())) {
                return command;
            }
        }
        clientHandler.sendMessage("[SERVER] Command not found, see all commands at /help");
        return null;
    }

    public abstract void execute(ClientHandler clientHandler, String arg) throws IOException;
}
