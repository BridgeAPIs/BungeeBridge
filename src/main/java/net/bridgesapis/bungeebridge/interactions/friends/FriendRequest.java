package net.bridgesapis.bungeebridge.interactions.friends;

import java.util.Date;
import java.util.UUID;

public class FriendRequest {

    private UUID from;
    private UUID to;
    private Date sendDate;

    public FriendRequest() {

    }

    public FriendRequest(UUID from, UUID to, Date sendDate) {
        this.from = from;
        this.to = to;
        this.sendDate = sendDate;
    }

    public UUID getFrom() {
        return from;
    }

    public UUID getTo() {
        return to;
    }
}
