package net.bridgesapis.bungeebridge.services;

import java.util.UUID;

/**
 * @author zyuiop
 */
public interface IFriendsManagement extends Service {
	boolean isFriend(UUID uniqueId, UUID id);
}
