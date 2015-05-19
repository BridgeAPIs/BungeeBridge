Documentation des protocoles PUBSUB BungeeBridge
=============================================

1. Avant toute chose
--------------------

### A. APIs PubSub

Le BukkitBridge inclus un gestionnaire de publishing/subscribing. Il dispose d'une queue de messages qu'il envoie l'un après l'autre.
Pour l'utiliser : `BukkitBridge.get().getPubSub().send(PendingMessage message)`

### B. A propos de cette documentation 

La documentation sera formattée comme suit :
`<channel> <argument 1> <argument 2> ... <argument n> [argument facultatif]`


## 2. Commandes APIExec

Les commandes APIExec ont pour but de permettre l'exécution de méthodes BungeeCord relatives aux joueurs depuis un environnement spigot.
La classe `ProxiedPlayer` du BukkitBridge est une implémentation de cette API.

*Appel :* n'importe quelle source
*Exécution :* proxy du joueur


1. `apiexec.send <uuid joueur> <TextComponent sérialisé en JSON>`
Permet d'envoyer un message à un joueur en passant par le proxy.

2. `apiexec.kick <uuid joueur> <TextComponent sérialisé en JSON>`
Déconnecte le joueur du proxy en lui affichant le TextComponent fourni comme motif.

3. `apiexec.connect <uuid joueur> <nom de serveur>`
Connecte le joueur au serveur passé en argument.

4. `apiexec.friendrequest <uuid de la source> <uuid de la destination>`
Envoie une demande en ami de `<source>` à `<destination>`

## 3. Exécution distante de commandes

Il est possible d'exécuter une commande Spigot/BungeeCord à distance. Elle sera exécutée en tant que console.

`commands.<type de destination>.<nom de destination> <commande>`

* Type de destination : `proxies` ou `servers`
* Nom de destination : `all` ou un nom de proxy/serveur

## 4. Communications

1. `globmessages.<pseudo emmeteur> <message>`
Envoie un message global sur tout le serveur. Le pseudo emmeteur ne doit pas nécessairement correspondre à un joueur réel.

2. `moderationchan <JSON Mod Message>`
Broadcaste un message sur le salon de modération. Utilisez la classe JsonModMessage (prochainement dans l'api/tools)

3. `privatemessages <JsonPrivateMessage>`
Envoie un message privé.

## 5. Redémarrage de serveur

S'exécute sur serveurs spigot uniquement.
`<salon> <reboot|rebootIfEmpty>`
* reboot : redémarre le serveur
* rebootIfEmpty : redémarre le serveur s'il est vide

Deux salons permettent de communiquer :
`global` : touche tous les serveurs
`<nom du serveur>` : touche un seul serveur

