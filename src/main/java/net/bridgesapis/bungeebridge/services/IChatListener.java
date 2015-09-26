package net.bridgesapis.bungeebridge.services;

import java.util.Date;
import java.util.UUID;

/**
 * @author zyuiop
 */
public interface IChatListener extends Service {
	void addMute(UUID id, Date end, String reason);

	void removeMute(UUID id);

	boolean isMuted(UUID id);

	String getReason(UUID id);

	Date getEnd(UUID id);

	void consume(String channel, String message);
}
