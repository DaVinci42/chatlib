package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.irc.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TopicCommandHandler implements CommandHandler {

    public static final int RPL_NOTOPIC = 331;
    public static final int RPL_TOPIC = 332;

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { "TOPIC", RPL_NOTOPIC, RPL_TOPIC };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        int numeric = CommandHandler.toNumeric(command);
        String topic = null;
        boolean isTopicCommand = command.equals("TOPIC");
        if (numeric == RPL_TOPIC)
            topic = params.get(2);
        else if (isTopicCommand)
            topic = params.get(1);
        try {
            ChannelData channelData = connection.getJoinedChannelData(params.get(isTopicCommand ? 0 : 1));

            MessageSenderInfo senderInfo = null;
            if (command.equals("TOPIC")) {
                try {
                    UUID userUUID = connection.getUserInfoApi().resolveUser(sender.getNick(), sender.getUser(),
                            sender.getHost(), null, null).get();
                    senderInfo = sender.toSenderInfo(userUUID, channelData);
                } catch (InterruptedException | ExecutionException ignored) {
                }
            }

            channelData.addMessage(new MessageInfo.Builder(senderInfo, topic, MessageInfo.MessageType.TOPIC), tags);
            channelData.callMemberListChanged();
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a topic message", e);
        }
    }

}
