package net.zyuiop.bungeebridge;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.zyuiop.bungeebridge.core.TasksExecutor;
import net.zyuiop.bungeebridge.core.handlers.ApiExecutor;
import net.zyuiop.bungeebridge.core.handlers.PubSubConsumer;
import net.zyuiop.bungeebridge.core.proxies.NetworkBridge;
import net.zyuiop.bungeebridge.core.database.DatabaseConnector;
import net.zyuiop.bungeebridge.core.players.PlayerDataManager;
import net.zyuiop.bungeebridge.core.database.Publisher;
import net.zyuiop.bungeebridge.core.database.ServerSettings;
import net.zyuiop.bungeebridge.interactions.friends.FriendsManagement;
import net.zyuiop.bungeebridge.interactions.parties.PartiesCommand;
import net.zyuiop.bungeebridge.interactions.parties.PartiesManager;
import net.zyuiop.bungeebridge.interactions.privatemessages.CommandMsg;
import net.zyuiop.bungeebridge.interactions.privatemessages.CommandReply;
import net.zyuiop.bungeebridge.interactions.privatemessages.PrivateMessagesHandler;
import net.zyuiop.bungeebridge.interactions.privatemessages.PrivateMessagesManager;
import net.zyuiop.bungeebridge.listeners.ChatListener;
import net.zyuiop.bungeebridge.listeners.PlayerLeaveEvent;
import net.zyuiop.bungeebridge.listeners.ServerMovementsListener;
import net.zyuiop.bungeebridge.lobbys.LobbySwitcher;
import net.zyuiop.bungeebridge.moderation.ModMessageHandler;
import net.zyuiop.bungeebridge.core.players.UUIDTranslator;
import net.zyuiop.bungeebridge.core.servers.ServersManager;
import net.zyuiop.bungeebridge.moderation.commands.*;
import net.zyuiop.bungeebridge.permissions.PermissionsBridge;
import net.zyuiop.bungeebridge.commands.*;
import net.zyuiop.bungeebridge.listeners.PlayerJoinEvent;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BungeeBridge extends Plugin {

	private static BungeeBridge instance;
	private DatabaseConnector connector;
	private String proxyName;
	private UUIDTranslator uuidTranslator;
	private Publisher publisher;
	private TasksExecutor executor;
	private PlayerDataManager playerDataManager;
	private ServerSettings serverSettings;
	private NetworkBridge networkBridge;
	private ServersManager serversManager;
	private LobbySwitcher lobbySwitcher;
	private FriendsManagement friendsManagement;
	private PartiesManager partiesManager;
	private ChatListener chatListener;
	private PrivateMessagesManager privateMessagesManager;
	private PermissionsBridge permissionsBridge;

	public void onEnable() {
		try {
			instance = this;

			getLogger().info("#===========[BungeeBridge Loading]===========#");
			getLogger().info("# Welcome to BungeeBridge ! The plugin wil   #");
			getLogger().info("# now load. Please be careful to the plugin  #");
			getLogger().info("# output. (c) zyuiop 2015                    #");
			getLogger().info("#===========[BungeeBridge Loading]===========#");


			Configuration config = loadConfiguration();
			proxyName = config.getString("proxyname");

			loadDatabase();

			publisher = new Publisher(connector);
			executor = new TasksExecutor();
			playerDataManager = new PlayerDataManager(this);
			serverSettings = new ServerSettings();
			serverSettings.refresh();
			networkBridge = new NetworkBridge(this);
			lobbySwitcher = new LobbySwitcher(this);
			serversManager = new ServersManager(this);
			privateMessagesManager = new PrivateMessagesManager(this);
			chatListener = new ChatListener(this);
			partiesManager = new PartiesManager(this);
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
			connector.subscribe("privatemessages", new PrivateMessagesHandler(privateMessagesManager));

			friendsManagement = new FriendsManagement(this);

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
			getProxy().getPluginManager().registerCommand(this, new CommandLobby(this));
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

			getProxy().getPluginManager().registerCommand(this, new CommandMsg(this));
			getProxy().getPluginManager().registerCommand(this, new CommandReply(this));
			getProxy().getPluginManager().registerCommand(this, new PartiesCommand(partiesManager, this));

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

	public LobbySwitcher getLobbySwitcher() {
		return lobbySwitcher;
	}

	public PartiesManager getPartiesManager() {
		return partiesManager;
	}

	@Override
	public void onDisable() {
		getLogger().info("[Disabling] Kicking parties...");
		for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
			partiesManager.leave(player.getUniqueId());
		}

		partiesManager.forceLeaveAll();

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

	public void loadDatabase() {
		File dataFile = new File(getDataFolder().getAbsoluteFile().getParentFile().getParentFile(), "data.yml");

		if (!dataFile.exists()) {
			this.getLogger().log(Level.SEVERE, "BungeeBridge stopped loading : data.yml not found");
			this.getProxy().stop();
			return;
		}

		try {
			Configuration conf = ConfigurationProvider.getProvider(YamlConfiguration.class).load(dataFile);
			Set<String> ips = ((List<String>) conf.getList("Redis-Ips")).stream().map(IP -> IP).collect(Collectors.toSet());
			connector = new DatabaseConnector(this, ips, conf.getString("mainMonitor", "mymaster"), conf.getString("cacheMonitor", "cache"),  conf.getString("Redis-Pass"));
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public PrivateMessagesManager getPrivateMessagesManager() {
		return privateMessagesManager;
	}
}
