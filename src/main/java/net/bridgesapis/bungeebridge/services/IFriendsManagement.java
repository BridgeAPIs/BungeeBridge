package net.bridgesapis.bungeebridge.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author zyuiop
 */
public interface IFriendsManagement extends Service {
	String sendRequest(UUID from, UUID add);

	boolean isFriend(UUID from, UUID isFriend);

	String grantRequest(UUID from, UUID add);

	String denyRequest(UUID from, UUID add);

	String removeFriend(UUID asking, UUID askTo);

	ArrayList<String> friendList(UUID asking);

	ArrayList<UUID> UUIDFriendList(UUID asking);

	HashMap<UUID, String> associatedFriendsList(UUID asking);

	HashMap<UUID, String> onlineAssociatedFriendsList(UUID asking);

	ArrayList<String> requestsList(UUID asking);

	ArrayList<String> sentRequestsList(UUID asking);

	void request(UUID from, UUID to, Date date);

	void response(UUID from, UUID to, boolean accepted);
}
