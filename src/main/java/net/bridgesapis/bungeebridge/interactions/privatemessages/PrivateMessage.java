package net.bridgesapis.bungeebridge.interactions.privatemessages;

import net.bridgesapis.bungeebridge.utils.UnknownPlayer;

public class PrivateMessage {

    private UnknownPlayer sender;
    private UnknownPlayer receiver;
    private String message;

    public PrivateMessage(UnknownPlayer sender, UnknownPlayer receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public PrivateMessage() {
    }

    public UnknownPlayer getSender() {
        return sender;
    }

    public UnknownPlayer getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }
}
