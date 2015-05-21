BungeeBridge
============

BungeeBridge, a reliable cross bungee platform.

Why BungeeBridge ?
------------------

BungeeBridge is great if you're planning to develop a server with multiple bungeecord proxies linked together.
It allows you to link these proxies through a Redis databases cluster.


What is required ?
------------------

BungeeBridge is a BungeeCord plugin, so you need at least one bungeecord server to run it.
You also need at lease two redis DataBases and one redis sentinel.
The sentinel **MUST** use two different masters :
 * A storage database, which will keep all the datas of your players
 * A cache database, which will keep running data
You also have to use our bukkit side bridge, BukkitBridge. This bridge will take care of registering a bukkitserver to the proxy, and a lot of other cool stuff.
This BukkitBridge is designed to be used as an API, so feel free to use it in your plugins.
It includes usefull tools to build a network.

How does it work ?
------------------

Proxies are "linked" through a database.
When a player joins or leave, a pubsub message is sent and the current amount of players is stored in the database.
Every 15 seconds, each proxy send a *keepalive* packet to the others. If a proxy doesn't send any keepalive during more than 30 seconds, it's declared as dead and automatically removed from the system.
When a bukkit server with BukkitBridge starts, it'll send a *keepalive* too. The proxies will add the server into the server list. If a server doesn't send any keepalive during more than 2 minutes, it's declared as offline and removed.

How can I use it ?
------------------

Download binaries : (http://gnomino.eu:8080/)

You can build the plugin :
* Build CrossPermissionsAPI
* Clone this repo
* Run `mvn install`

**You have to use the -with-dependencies jar file !**

How can I help ?
----------------

Feel free to fork this repo and do pull requests. There is a TodoList into the CONTRIBUTE.MD file.