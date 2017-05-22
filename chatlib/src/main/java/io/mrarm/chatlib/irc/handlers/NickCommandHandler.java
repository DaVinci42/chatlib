package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.user.UserInfo;

import java.util.concurrent.ExecutionException;

public class NickCommandHandler implements CommandHandler {

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, String params)
            throws InvalidMessageException {
        if (sender.getNick().equals(connection.getUserNick()))
            connection.onChannelJoined(params);
        try {
            UserInfo info = connection.getUserInfoApi().getUser(sender.getNick(), sender.getUser(), sender.getHost(),
                    null, null).get();
            connection.getUserInfoApi().notifyNickChange(info, params);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
