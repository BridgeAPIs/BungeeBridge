package net.bridgesapis.bungeebridge;

import net.bridgesapis.bungeebridge.commands.*;
import net.bridgesapis.bungeebridge.core.TasksExecutor;
import net.bridgesapis.bungeebridge.core.database.*;
import net.bridgesapis.bungeebridge.core.handlers.ApiExecutor;
import net.bridgesapis.bungeebridge.core.players.PlayerDataManager;
import net.bridgesapis.bungeebridge.core.proxies.NetworkBridge;
import net.bridgesapis.bungeebridge.core.servers.ServersManager;
import net.bridgesapis.bungeebridge.i18n.I18n;
import net.bridgesapis.bungeebridge.interactions.privatemessages.CommandMsg;
import net.bridgesapis.bungeebridge.interactions.privatemessages.PrivateMessagesHandler;
import net.bridgesapis.bungeebridge.listeners.ChatListener;
import net.bridgesapis.bungeebridge.listeners.PlayerLeaveEvent;
import net.bridgesapis.bungeebridge.lobbys.LobbyManager;
import net.bridgesapis.bungeebridge.moderation.ModMessageHandler;
import net.bridgesapis.bungeebridge.moderation.commands.*;
import net.bridgesapis.bungeebridge.permissions.PermissionsBridge;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.bridgesapis.bungeebridge.core.handlers.PubSubConsumer;
import net.bridgesapis.bungeebridge.interactions.friends.FriendsManagement;
import net.bridgesapis.bungeebridge.interactions.parties.PartiesCommand;
import net.bridgesapis.bungeebridge.interactions.parties.PartiesManager;
import net.bridgesapis.bungeebridge.interactions.privatemessages.CommandReply;
import net.bridgesapis.bungeebridge.interactions.privatemessages.PrivateMessagesManager;
import net.bridgesapis.bungeebridge.listeners.ServerMovementsListener;
import net.bridgesapis.bungeebridge.core.players.UUIDTranslator;
import net.bridgesapis.bungeebridge.listeners.PlayerJoinEvent;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BungeeBridge extends Plugin {

	private static BungeeBridge instance;
	private DatabaseConnector connector;
	private Configuration configuration;
	private String proxyName;
	private UUIDTranslator uuidTranslator;
	private Publisher publisher;
	private TasksExecutor executor;
	private PlayerDataManager playerDataManager;
	private ServerSettings serverSettings;
	private NetworkBridge networkBridge;
	private ServersManager serversManager;
	private ChatListener chatListener;
	private PermissionsBridge permissionsBridge;

	private PrivateMessagesManager privateMessagesManager = null;
	private FriendsManagement friendsManagement = null;
	private PartiesManager partiesManager = null;
	private LobbyManager lobbySwitcher = null;

	public void onEnable() {
		try {
			instance = this;

			getLogger().info("#===========[BungeeBridge Loading]===========#");
			getLogger().info("# Welcome to BungeeBridge ! The plugin wil   #");
			getLogger().info("# now load. Please be careful to the plugin  #");
			getLogger().info("# output. (c) zyuiop 2015                    #");
			getLogger().info("#===========[BungeeBridge Loading]===========#");


			configuration = loadConfiguration();
			proxyName = configuration.getString("proxyname");

			I18n.load(configuration.getString("lang", "default"), this);

			loadDatabase(configuration);

			publisher = new Publisher(connector);
			executor = new TasksExecutor();
			playerDataManager = new PlayerDataManager(this);
			serverSettings = new ServerSettings();
			serverSettings.refresh();
			networkBridge = new NetworkBridge(this);


			serversManager = new ServersManager(this);
			chatListener = new ChatListener(this);

			permissionsBridge = new PermissionsBridge(this);

			PubSubConsumer commands = (channel, message) -> {
				ProxyServer.getInstance().getLogger().info("Executing remote command : " + message);
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), message);
			};

			connector.subscribe("commands.proxies.all", commands);
			connector.psubscribe("apiexec.*", new ApiExecutor());
			connector.subscribe("commands.proxies." + getProxyName(), commands);
			connector.subscribe("moderationchan", new ModMessageHandler());
			connector.psubscribe("mute.*", chatListener);
			connector.psubscribe("globmessages.*", new GlobalMessagesHandler());

			if (configuration.getBoolean("modules.privatemessages.enabled", true)) {
				privateMessagesManager = new PrivateMessagesManager(this);
				connector.subscribe("privatemessages", new PrivateMessagesHandler(privateMessagesManager));
				getProxy().getPluginManager().registerCommand(this, new CommandMsg(this));
				getProxy().getPluginManager().registerCommand(this, new CommandReply(this));
			}

			if (configuration.getBoolean("modules.parties.enabled", true)) {
				partiesManager = new PartiesManager(this);
				getProxy().getPluginManager().registerCommand(this, new PartiesCommand(partiesManager, this));
			}

			if (configuration.getBoolean("modules.lobbies.enabled", true)) {
				lobbySwitcher = new LobbyManager(this, configuration.getString("modules.lobbies.lobbyprefix", "Lobby_"));
				getProxy().getPluginManager().registerCommand(this, new CommandLobby(this));
			}

			if (configuration.getBoolean("modules.friends.enabled", true)) {
				friendsManagement = new FriendsManagement(this);
			}

			ProxyServer.getInstance().getScheduler().schedule(this, serverSettings::refresh, 30, 30, TimeUnit.SECONDS);

			new Thread(publisher, "PublisherThread").start();
			new Thread(executor, "ExecutorThread").start();

			uuidTranslator = new UUIDTranslator(this);


			Jedis jedis = connector.getResource();
			for (ListenerInfo info : ProxyServer.getInstance().getConfig().getListeners()) {
				String ipStr = info.getHost().getAddress().getHostAddress();
				jedis.sadd("proxys", ipStr);
			}
			jedis.close();

			// Commandes
			getProxy().getPluginManager().registerCommand(this, new CommandDispatch());
			getProxy().getPluginManager().registerCommand(this, new CommandGlist(this));
			getProxy().getPluginManager().registerCommand(this, new CommandLocation(this));
			getProxy().getPluginManager().registerCommand(this, new CommandProxyDebug(this));
			getProxy().getPluginManager().registerCommand(this, new CommandSetOption(this));
			getProxy().getPluginManager().registerCommand(this, new CommandHelp());
			getProxy().getPluginManager().registerCommand(this, new CommandGlobal(this));
			getProxy().getPluginManager().registerCommand(this, new CommandSetAutoMessage());
			getProxy().getPluginManager().registerCommand(this, new CommandDelAutoMessage());

			getProxy().getPluginManager().registerCommand(this, new CommandBan(this));
			getProxy().getPluginManager().registerCommand(this, new CommandBTP(this));
			getProxy().getPluginManager().registerCommand(this, new CommandKick(this));
			getProxy().getPluginManager().registerCommand(this, new CommandMod(this));
			getProxy().getPluginManager().registerCommand(this, new CommandMute(this));
			getProxy().getPluginManager().registerCommand(this, new CommandModpass());
			getProxy().getPluginManager().registerCommand(this, new CommandPardon(this));
			getProxy().getPluginManager().registerCommand(this, new CommandSTP(this));

			getProxy().getPluginManager().registerListener(this, new ServerMovementsListener(this));
			getProxy().getPluginManager().registerListener(this, new PlayerJoinEvent(this));
			getProxy().getPluginManager().registerListener(this, new PlayerLeaveEvent(this));
			getProxy().getPluginManager().registerListener(this, chatListener);

			// Schedule
			getProxy().getScheduler().schedule(this, () -> {
				Jedis j = getConnector().getResource();
				String message = j.get("automessage");
				j.close();
				if (message != null)
					ProxyServer.getInstance().broadcast(TextComponent.fromLegacyText(message.replace("&", "§")));
			}, 15, 15, TimeUnit.MINUTES);

			getLogger().info("#===========[BungeeBridge Loaded]===========#");
			getLogger().info("# The BungeeBridge was successfully loaded. #");
			getLogger().info("# Use /pdebug to have more information      #");
			getLogger().info("# about pubsub exchanged messages.          #");
			getLogger().info("#===========[BungeeBridge Loaded]===========#");

			serverSettings.setAllowJoin(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PermissionsBridge getPermissionsBridge() {
		return permissionsBridge;
	}

	public ChatListener getChatListener() {
		return chatListener;
	}

	public FriendsManagement getFriendsManagement() {
		return friendsManagement;
	}

	public LobbyManager getLobbySwitcher() {
		return lobbySwitcher;
	}

	public PartiesManager getPartiesManager() {
		return partiesManager;
	}

	public boolean hasLobbySwitcher() {
		return lobbySwitcher != null;
	}

	@Override
	public void onDisable() {
		getLogger().info("[Disabling] Kicking parties...");
		if (partiesManager != null) {
			for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
				partiesManager.leave(player.getUniqueId());
			}
			partiesManager.forceLeaveAll();
		}



		getLogger().info("[Disabling] Unregistering proxy...");

		Jedis jedis = connector.getResource();
		for (ListenerInfo info : ProxyServer.getInstance().getConfig().getListeners()) {
			String ipStr = info.getHost().getAddress().getHostAddress();
			jedis.srem("proxys", ipStr);
		}


		getLogger().info("[Disabling] Clearing cached data...");
		jedis.del("famouslocations");

		jedis.close();

		getLogger().info("[Disabling] Disabling proxies...");
		networkBridge.disable();
		getLogger().info("[Disabling] Disabling serversmanager...");
		serversManager.disable();

		getLogger().info("[Disabling] Disabling connector...");
		connector.disable();

		getLogger().info("Disabled plugin");
	}

	public NetworkBridge getNetworkBridge() {
		return networkBridge;
	}

	public ServersManager getServersManager() {
		return serversManager;
	}

	public ServerSettings getServerSettings() {
		return serverSettings;
	}

	public PlayerDataManager getPlayerDataManager() {
		return playerDataManager;
	}

	public TasksExecutor getExecutor() {
		return executor;
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public String getProxyName() {
		return proxyName;
	}

	public UUIDTranslator getUuidTranslator() {
		return uuidTranslator;
	}

	public DatabaseConnector getConnector() {
		return connector;
	}

	public void loadDatabase(Configuration configuration) {
		if (configuration.get("singleredis.masterhost") != null) {
			getLogger().info("Using single redis instance. Comment 'singleredis.masterhost' node to disable and switch to sentinel network");
			getLogger().warning("This feature is still in test, it may encounter problems !");
			String master = configuration.getString("singleredis.masterhost");
			String cache = configuration.getString("singleredis.cachehost");
			String auth = configuration.getString("singleredis.auth");
			connector = new SingleDatabaseConnector(this, master, cache, auth);
			return;
		}

		getLogger().info("Using a sentinel based database.");
		List<String> ips = configuration.getStringList("database.sentinels");
		HashSet<String> ipsSet = new HashSet<>();
		ipsSet.addAll(ips);
		String password = configuration.getString("database.auth");
		String master = configuration.getString("database.mastername");
		String cacheMaster = configuration.getString("database.cachemastername");
		connector = new SentinelDatabaseConnector(this, ipsSet, master, cacheMaster, password);
	}

	public Configuration loadConfiguration() throws IOException {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();

		File file = new File(getDataFolder(), "config.yml");

		if (!file.exists()) {
			Files.copy(getResourceAsStream("config.yml"), file.toPath());
		}

		return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
	}

	public static BungeeBridge getInstance() {
		return instance;
	}

	public boolean hasFriends() {
		return friendsManagement != null;
	}

	public PrivateMessagesManager getPrivateMessagesManager() {
		return privateMessagesManager;
	}
}
